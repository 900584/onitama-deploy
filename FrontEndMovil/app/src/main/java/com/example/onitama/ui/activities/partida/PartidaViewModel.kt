package com.example.onitama.ui.activities.partida

import androidx.lifecycle.ViewModel
import com.example.onitama.PartidaActiva
import com.example.onitama.lib.Carta
import com.example.onitama.lib.EstadoJuego
import com.example.onitama.lib.Posicion
import com.example.onitama.lib.calcularMovimientosValidos
import com.example.onitama.lib.crearEstadoInicial
import com.example.onitama.lib.crearEstadoServidor
import com.example.onitama.lib.ejecutarMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PartidaViewModel : ViewModel() {

    private val _estado = MutableStateFlow(configurarEstadoInicial())
    val estado: StateFlow<EstadoJuego> = _estado.asStateFlow()

    private fun configurarEstadoInicial(): EstadoJuego {
        val datos = PartidaActiva.datosPartida

        return if (datos != null) {
            // PARTIDA PÚBLICA: Usamos los datos del servidor
            crearEstadoServidor(
                cartas_jugador = datos.cartas_jugador,
                cartas_oponente = datos.cartas_oponente,
                carta_siguiente = datos.carta_siguiente,
                equipo = datos.obtenerEquipoID()
            )
        } else {
            // PARTIDA LOCAL: Contra el Bot
            crearEstadoInicial()
        }
    }


    fun tocarCelda(pos: Posicion) {
        val actual = _estado.value

        // Si ya hay algo seleccionado y el destino es válido, movemos
        if (actual.movimientosValidos.contains(pos) && actual.fichaSeleccionada != null && actual.cartaSeleccionada != null) {
            val resultado = ejecutarMovimiento(
                actual,
                actual.fichaSeleccionada,
                pos,
                actual.cartaSeleccionada
            )
            _estado.value = resultado.nuevoEstado
        }
        else if(actual.cartaSeleccionada != null){
            val celda = actual.tablero[pos.fila][pos.col]
            if (celda.ficha?.equipo == actual.turnoActual) {
                _estado.value = actual.copy(
                    fichaSeleccionada = pos,
                    movimientosValidos = calcularMovimientosValidos(actual.tablero, pos.fila, pos.col, actual.cartaSeleccionada!!, actual.turnoActual)
                )
            }
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
                actual.turnoActual
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

    fun desSeleccionarCarta(carta: Carta) {
        val actual = _estado.value
        _estado.value = actual.copy(cartaSeleccionada = null, fichaSeleccionada = null, movimientosValidos = emptyList())

    }

    private fun calcularNuevosMovimientos(estado: EstadoJuego, carta: Carta?, pos: Posicion?): List<Posicion> {
        if (carta == null || pos == null) return emptyList()
        return calcularMovimientosValidos(
            estado.tablero,
            pos.fila,
            pos.col,
            carta,
            estado.tablero[pos.fila][pos.col].ficha!!.equipo
        )
    }
}