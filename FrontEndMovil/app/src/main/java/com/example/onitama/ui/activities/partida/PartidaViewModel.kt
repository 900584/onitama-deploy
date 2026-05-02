package com.example.onitama.ui.activities.partida

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onitama.PartidaActiva
import com.example.onitama.api.ManejadorGlobal
import com.example.onitama.api.Partida
import com.example.onitama.api.jsonPartida
import com.example.onitama.lib.Carta
import com.example.onitama.lib.Cartas
import com.example.onitama.lib.Dificultad
import com.example.onitama.lib.EquipoID
import com.example.onitama.lib.EstadoJuego
import com.example.onitama.lib.FasePartida
import com.example.onitama.lib.JugadaIA
import com.example.onitama.lib.ModoJuego
import com.example.onitama.lib.Posicion
import com.example.onitama.lib.calcularMejorMovimientoIA
import com.example.onitama.lib.calcularMovimientosValidos
import com.example.onitama.lib.crearEstadoInicial
import com.example.onitama.lib.crearEstadoServidor
import com.example.onitama.lib.ejecutarMovimiento
import com.example.onitama.lib.invertirCartasEspejo
import com.example.onitama.lib.activarRestriccionSolo
import com.example.onitama.lib.TipoRestriccion
import com.example.onitama.AutoLogin
import com.example.onitama.lib.aplicarCartaAccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PartidaViewModel : ViewModel() {

    val END = 6;
    var modoJuegoActual: ModoJuego = ModoJuego.BOT
        private set
    var nivelDificultadBot: Dificultad = Dificultad.FACIL
        private set

    private val _estado = MutableStateFlow(crearEstadoInicial())
    var razon: String? = null
    val estado: StateFlow<EstadoJuego> = _estado.asStateFlow()

    private var cartaAccionEnUso: String? = null

    private val _notificacionPausa = MutableStateFlow<Partida.RespuestaSolicitudPausa?>(null)
    val notificacionPausa = _notificacionPausa.asStateFlow()

    var partida = Partida()

    var equipoPropio = EquipoID.AZUL //de momento el bot siempre es el rojo, ya si eso se mejorará más adelante

    var limpiar: (() -> Unit)? = null

    fun iniciarPartida(modo: ModoJuego, dificultad: Dificultad = Dificultad.FACIL) {
        modoJuegoActual = modo
        nivelDificultadBot = dificultad

        val datos = PartidaActiva.datosPartida

        if (modo == ModoJuego.PUBLICA || modo == ModoJuego.PRIVADA) {
            Log.i("INFORMACION PARTIDA INICIADA", "{s}")
            if (datos != null) {
                equipoPropio = if (datos.equipo == 1) EquipoID.AZUL else EquipoID.ROJO

                // Primero construimos el tablero con las cartas del servidor
                val esNueva = (modo == ModoJuego.PUBLICA || modo == ModoJuego.PRIVADA )
                _estado.value = crearEstadoServidor(
                    cartas_jugador = datos.cartas_jugador.map { it.nombre },
                    cartas_oponente = datos.cartas_oponente.map { it.nombre },
                    carta_siguiente = datos.carta_siguiente.map { it.nombre },
                    tablero_eq1 = datos.tablero_eq1,
                    tablero_eq2 = datos.tablero_eq2,
                    turno = datos.turno,
                    cartas_accion_propia = datos.cartas_accion_jugador,
                    cartas_accion_rival = datos.cartas_accion_oponente,
                    esReanudada = !esNueva
                )

                // Luego conectamos el WebSocket para escuchar los turnos
                conectarAlServidor()
            }
        } else {
            // Es contra el Bot
            equipoPropio = EquipoID.AZUL // El jugador local
            _estado.value = crearEstadoInicial()
        }
    }

    private fun conectarAlServidor() {


        val sePudoEnviar = partida.enviarEstoyListo()
        Log.w("CHIVATO_WS", "¿Se pudo enviar ESTOY_LISTO?: $sePudoEnviar")

        if (sePudoEnviar) {
            viewModelScope.launch {
                ManejadorGlobal.mensajesEntrantes.collect { json ->
                    val jsonTolerante = Json {
                        ignoreUnknownKeys = true
                        classDiscriminator = "tipo"
                    }

                    try {

                        val mensaje = jsonTolerante.decodeFromString<Partida.MensajeServidor>(json.toString())

                        Log.w("CHIVATO_WS", "El ViewModel está procesando: $mensaje")

                        val actual = _estado.value
                        when (mensaje) {
                            is Partida.RespuestaTuTurno -> {
                                _estado.value = actual.copy(turnoActual = equipoPropio)
                            }

                            is Partida.RespuestaMover -> {
                                val filaOrigen = END - mensaje.fila_origen
                                val colOrigen = END - mensaje.col_origen
                                val origen = Posicion(filaOrigen, colOrigen)

                                val filaDestino = END - mensaje.fila_destino
                                val colDestino = END - mensaje.col_destino
                                val destino = Posicion(filaDestino, colDestino)
                                val carta = Cartas.getCarta(mensaje.carta)

                                Log.i("conexion servidor", "Mensaje de movimiento recibido")

                                val resultado = ejecutarMovimiento(
                                    estado = actual, 
                                    origen, 
                                    destino, 
                                    carta, 
                                    equipoPropio, 
                                    trampaActivada = mensaje.trampa_activada?: false
                                )

                                if(resultado.victoriaPorTrono){
                                    razon = "TRONO"
                                }
                                if(resultado.esReyCapturado) {
                                    razon = "REY CAPTURADO"
                                }
                                _estado.value = resultado.nuevoEstado//.copy(turnoActual = equipoPropio)
                            }

                            is Partida.RespuestaMovimientoInvalido -> {
                                Log.e("Partida", "Error: Movimiento inválido")
                                desSeleccionarCarta()
                            }

                            is Partida.RespuestaDerrota -> {
                                razon = if (mensaje.motivo == "SIN_MOV") {
                                    "SIN_MOVIMIENTOS"
                                }
                                else {
                                    "DERROTA"
                                }

                                _estado.value = _estado.value.copy(
                                    ganador = if (equipoPropio == EquipoID.AZUL) EquipoID.ROJO else EquipoID.AZUL,
                                    fasePartida = FasePartida.TERMINADA
                                )
                                Log.i("conexion servidor", "Mensaje de derrota recibido")
                            }

                            is Partida.RespuestaVictoria -> {
                                if(mensaje.motivo == "ABANDONO"){
                                    razon = "ABANDONO"
                                }
                                else if(mensaje.motivo == "SIN_MOV"){
                                    razon = "SIN_MOVIMIENTOS"
                                }
                                else {
                                    razon = "VICTORIA"
                                }
                                _estado.value = _estado.value.copy(
                                    ganador = if (equipoPropio == EquipoID.ROJO) EquipoID.ROJO else EquipoID.AZUL,
                                    fasePartida = FasePartida.TERMINADA
                                )
                                Log.i("conexion servidor", "Mensaje de victoria recibido")
                            }

                            is Partida.RespuestaTerminarPartida ->{
                                if (mensaje.razon == "ABANDONO"){ razon ="ABANDONO" }
                                Log.i("conexion servidor", "Mensaje de terminar partida recibido")
                                _estado.value = _estado.value.copy(
                                    ganador = if (mensaje.ganador == EquipoID.ROJO.id.toString()) EquipoID.ROJO else EquipoID.AZUL,
                                    fasePartida = FasePartida.TERMINADA
                                )
                            }

                            is Partida.RespuestaSolicitudPausa -> {
                                _notificacionPausa.value = mensaje
                            }

                            is Partida.RespuestaPartidaPausada -> { }

                            is Partida.RespuestaPausaRechazada -> { }

                            is Partida.RespuestaPartidaLista -> {
                                _estado.value = actual.copy(
                                    fasePartida = FasePartida.JUGANDO,
                                    cartasAccionPropia = mensaje.cartas_accion.map { it.nombre }
                                )
                            }

                            is Partida.RespuestaSeleccioneCartaAccion -> {
                                _estado.value = actual.copy(
                                    fasePartida = FasePartida.ELEGIR_CARTA_ACCION,
                                    cartasAccionPropia = mensaje.cartas_accion.map { it.nombre }
                                )
                            }


                            is Partida.RespuestaTrampaActivada -> {
                                val fila = END - mensaje.fila
                                val columna = END - mensaje.columna

                                val nuevoTablero = actual.tablero.map {
                                    it.toMutableList()
                                }.toMutableList()

                                nuevoTablero[fila][columna] = nuevoTablero[fila][columna].copy(
                                    ficha = null,
                                    esTrampaEquipo = -1
                                )

                                _estado.value = actual.copy(
                                    tablero = nuevoTablero
                                )
                            }

                            is Partida.RespuestaCartaAccionJugada -> {
                                val jugador = if (mensaje.equipo == 1) {
                                    EquipoID.AZUL
                                }
                                else {
                                    EquipoID.ROJO
                                }

                                val tipoAccion = obtenerCartaAccion(mensaje.carta_accion)

                                _estado.value = aplicarCartaAccion(
                                    estado = actual,
                                    equipo = jugador,
                                    cartaNombre = mensaje.carta_accion,
                                    x = if (mensaje.x != -1) END - mensaje.x else -1,
                                    y = if (mensaje.y != -1) END - mensaje.y else -1,
                                    x_op = if (mensaje.x_op != -1) END - mensaje.x_op else -1,
                                    y_op = if (mensaje.y_op != -1) END - mensaje.y_op else -1,
                                    cartaRobar = mensaje.carta_robar,
                                    tipo = tipoAccion
                                )

                                if (jugador == equipoPropio) {
                                    cartaAccionEnUso = null
                                }
                            }

                            is Partida.RespuestaTrampaInvalida -> {
                                Log.e("Partida", "Error: No se puede poner una trampa en esa casilla")
                            }

                            is Partida.RespuestaCartaAccionInvalida -> {
                                Log.e("Partida", "Error: No puedes usar esta carta de acción ahora")
                            }                           

                            else -> {
                                println("LOG: Mensaje recibido no reconocido: $mensaje")
                            }
                        }
                    } catch (e: Exception) {
                        println("Mensaje ignorado (no pertenece a la lógica de partida)")
                    }
                }
            }
        }
    }

    fun tocarCelda(pos: Posicion) {
        val actual = _estado.value
        Log.d("LOG", "Toque en $pos durante fase ${actual.fasePartida}")
        when (actual.fasePartida) {
            FasePartida.COLOCAR_TRAMPA -> {

                val sePuede = if (equipoPropio == EquipoID.AZUL) {
                    pos.fila >= 4
                }
                else {
                    pos.fila <= 2
                }

                if (sePuede) {
                    partida.enviarPonerTrampa(
                        equipo = equipoPropio.id,
                        fila = END - pos.fila,
                        columna = END - pos.col
                    )
                }
            }

            FasePartida.JUGANDO -> {
                if (actual.modoAccion != null) {
                    Log.d("LOG", "modoaccion no es null")
                    val cartaAccion = actual.modoAccion ?: return
                    val nombreCarta = cartaAccionEnUso ?: return

                    when (cartaAccion) {
                        "REVIVIR" -> {
                            ejecucionCartaAccion(nombreCarta, cartaAccion, posicionPropia = pos)
                        }

                        "SACRIFICIO" -> {
                            ejecucionCartaAccion(nombreCarta, cartaAccion, posicionRival = pos)
                        }

                        "SALVAR_REY" -> {
                            ejecucionCartaAccion(nombreCarta, cartaAccion, posicionPropia = pos)
                        }

                        "ROBAR" -> {}
                    }
                }
                else {
                    Log.d("LOG", "modoaccion es null y el turno es ${actual.turnoActual}")
                    //si le toca al oponente se ignoran los clicks
                    if(actual.turnoActual == equipoPropio){
                        Log.d("LOG", "Nos toca")
                    // Si ya hay algo seleccionado y el destino es válido, movemos
                        if (actual.movimientosValidos.contains(pos) && actual.fichaSeleccionada != null && actual.cartaSeleccionada != null) {
                            Log.d("LOG", "Toque en $pos durante fase ${actual.fasePartida} con carta seleccionada ${actual.cartaSeleccionada.nombre} y la ficha seleccionada ${actual.fichaSeleccionada}")
                            val resultado = ejecutarMovimiento(
                                actual,
                                actual.fichaSeleccionada,
                                pos,
                                actual.cartaSeleccionada,
                                equipoPropio
                            )
                            if(resultado.victoriaPorTrono){
                                razon = "TRONO"
                            }
                            if(resultado.esReyCapturado){
                                razon = "REY CAPTURADO"
                            }
                            _estado.value = resultado.nuevoEstado

                            if (resultado.nuevoEstado.turnoActual != equipoPropio) {
                                if (modoJuegoActual == ModoJuego.BOT) {
                                    if (resultado.nuevoEstado.ganador == null) {
                                        jugarTurnoBot()
                                    }
                                }
                                else {
                                    partida.enviarMovimiento(
                                        Partida.MensajeMover(
                                            equipo = equipoPropio.id,
                                            col_origen = END - actual.fichaSeleccionada.col ,
                                            fila_origen = END - actual.fichaSeleccionada.fila ,
                                            col_destino = END - pos.col,
                                            fila_destino = END - pos.fila,
                                            carta = actual.cartaSeleccionada.nombre
                                        ))
                                }
                            }
                        }
                        else if (actual.cartaSeleccionada != null) {
                            Log.d("LOG", "Toque en $pos durante fase ${actual.fasePartida} con carta seleccionada ${actual.cartaSeleccionada.nombre}")
                            val celda = actual.tablero[pos.fila][pos.col]
                            Log.d("LOG", "en esa celda hay una ficha? ${celda.ficha != null}")
                            if (celda.ficha?.equipo == actual.turnoActual) {
                                Log.d("LOG", "en esa celda hay una ficha tuya, todo bien")
                                _estado.value = actual.copy(
                                    fichaSeleccionada = pos,
                                    movimientosValidos = calcularMovimientosValidos(
                                        actual.tablero, 
                                        pos.fila,
                                        pos.col, 
                                        actual.cartaSeleccionada, 
                                        actual.turnoActual,
                                        actual.restriccionSolo
                                    )
                                )
                            }
                        }
                    }else{
                        Log.d("LOG", "No nos toca")
                    }
                }
            }
            else -> {}
        }
    }

    fun elegirCartaAccionInicial(
        nombreCarta: String
    ) {
        val actual = _estado.value
        if (actual.fasePartida == FasePartida.ELEGIR_CARTA_ACCION) {
            partida.enviarSeleccionAccion(
                nombreCarta, 
                equipoPropio.id
            )
        }
    }

    fun seleccionarCarta(carta: Carta) {
        val actual = _estado.value


        if (actual.fichaSeleccionada != null) {
            // 1. Calculamos los movimientos con la ficha actual y la carta nueva
            val posibles = calcularMovimientosValidos(
                actual.tablero,
                actual.fichaSeleccionada.fila,
                actual.fichaSeleccionada.col,
                carta,
                actual.turnoActual,
                actual.restriccionSolo
            )

            println("LOG: Carta seleccionada -> ${carta.nombre}. Movimientos hallados: ${posibles.size}")

            // 2. Actualizamos el estado UNA SOLA VEZ con copy
            _estado.value = actual.copy(
                cartaSeleccionada = carta,
                movimientosValidos = posibles
            )
        } else {
            println("LOG: Carta seleccionada -> ${carta.nombre}.")
            // Como la idea es que se seleccione primero la carta y luego la ficha, pero los movimientos solo se reslatan si hay ficha, la carta se selecciona igualmente
            _estado.value = actual.copy(cartaSeleccionada = carta)
        }
    }

    fun desSeleccionarCarta() {
        val actual = _estado.value
        _estado.value = actual.copy(
            cartaSeleccionada = null, 
            fichaSeleccionada = null, 
            movimientosValidos = emptyList(),
            modoAccion = null
        )
    }

    fun activarCartaAccion (
        nombreCarta: String
    ) {
        val actual = _estado.value

        if (actual.turnoActual == equipoPropio) {
            val carta = obtenerCartaAccion(nombreCarta) ?: return

            cartaAccionEnUso = nombreCarta

            if (carta == "ESPEJO" || 
                carta == "CEGAR" ||
                carta == "SOLO_PARA_ADELANTE" ||
                carta == "SOLO_PARA_ATRAS") {
                ejecucionCartaAccion(nombreCarta, carta)
            }   
            else {
                _estado.value = actual.copy(
                    modoAccion = carta
                )
            }
        }
    }

    private fun obtenerCartaAccion(
        nombre: String
    ): String? {
        return when (nombre) {
            "Pensatorium" -> "ESPEJO"
            "Atrapasueños" -> "ROBAR"
            "Requiem" -> "SACRIFICIO"
            "Santo Grial" -> "REVIVIR"
            "La Dama del Mar" -> "SOLO_PARA_ADELANTE"
            "Finisterra" -> "SOLO_PARA_ATRAS"
            "Brujeria" -> "CEGAR"
            "Illusia" -> "SALVAR_REY"
            else -> null
        }
    }

    fun ejecucionCartaAccion(
        nombreCarta: String,
        cartaAccion: String,
        posicionPropia: Posicion? = null,
        posicionRival: Posicion? = null,
        cartaARobar: String = ""
    ) {
        val mensaje = Partida.MensajeJugarCartaAccion(
            nombreCarta,
            equipoPropio.id,
            x = if (posicionPropia != null) END - posicionPropia.col else -1,
            y = if (posicionPropia != null) END - posicionPropia.fila else -1,
            x_op = if (posicionRival != null) END - posicionRival.col else -1,
            y_op = if (posicionRival != null) END - posicionRival.fila else -1,
            cartaRobar = cartaARobar
        )

        val sePuedeJugar = partida.enviarJugarCartaAccion(mensaje)
        if (sePuedeJugar) {
            _estado.value = _estado.value.copy(
                modoAccion = null
            )
            cartaAccionEnUso = null
        }
    }

    fun seleccionarCartaRobar(
        nombreCarta: String
    ) {
        val cartaAccion = cartaAccionEnUso ?: return 

        ejecucionCartaAccion(
            nombreCarta = cartaAccion,
            cartaAccion = "ROBAR",
            cartaARobar = nombreCarta
        )
    }

    fun activarPausa() {
        val datos = PartidaActiva.datosPartida ?: return

        val jugador = AutoLogin.sesion.value?.nombre ?: "Jugador"
        val rival = datos.oponente
        val idPartida = datos.partida_id

        val exito = partida.enviarSolicitudPausa(
            jugador,
            rival,
            idPartida
        )
    }

    fun enviarAceptarPausa(
        idNotificacion: Int,
        miNombre: String
    ) {
        val exito = partida.enviarAceptarPausa(
            idNotificacion,
            miNombre
        )

        if (exito) {
            _notificacionPausa.value = null
        }
    }

    fun enviarRechazarPausa(
        idNotificacion: Int,
        miNombre: String
    ) {
        val exito = partida.enviarRechazarPausa(
            idNotificacion,
            miNombre
        )

        if (exito) {
            _notificacionPausa.value = null
        }
    }

    private fun jugarTurnoBot() {
        val estadoActual = _estado.value

        // Por seguridad, comprobamos que realmente es el turno del bot y no hay ganador
        val ia = if (equipoPropio == EquipoID.ROJO) EquipoID.AZUL else EquipoID.ROJO
        if (estadoActual.turnoActual != ia || estadoActual.ganador != null) return

        // Lanzamos una corrutina en el hilo Default (optimizado para cálculos pesados de IA)
        viewModelScope.launch(Dispatchers.Default) {

            delay(2000)
            //Calculamos la jugada
            var jugada = calcularMejorMovimientoIA(
                estado = estadoActual,
                // De momento así luego ya si eso añadimos un menú similar al que tenían las partidas privadas para elegir dificultad y equipo que quieres jugar
                equipoIA = ia,
                equipoLocal = equipoPropio,
                dificultad = nivelDificultadBot
            )
            Log.i("LOG BOT", "Jugada calculada: $jugada")


            // 3. Aplicamos la jugada en el hilo principal
            if (jugada != null) {
                withContext(Dispatchers.Main) {
                    aplicarJugadaEnEstado(jugada)
                }
            }
        }
    }

    private fun aplicarJugadaEnEstado(jugada: JugadaIA) {
        val actual = _estado.value

        val posicionOrigen = Posicion(jugada.origenFila, jugada.origenCol)
        val posicionDestino = Posicion(jugada.destinoFila, jugada.destinoCol)

        val resultado = ejecutarMovimiento(
            estado = actual,
            origen = posicionOrigen,
            destino = posicionDestino,
            carta = jugada.carta,
            equipoPropio
        )

        _estado.value = resultado.nuevoEstado
    }

    override fun onCleared() {
        super.onCleared()
        partida.desconectarPartida()
        println("LOG: ViewModel destruido, conexión WebSocket limpiada.")
    }

    fun botonAbandonar(){
        partida.enviarAbandono(equipoPropio.id)
        razon = "ABANDONO"
    }

}