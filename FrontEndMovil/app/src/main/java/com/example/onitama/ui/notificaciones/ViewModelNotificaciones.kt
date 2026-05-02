package com.example.onitama.ui.notificaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onitama.api.Amigos
import com.example.onitama.api.ManejadorGlobal
import com.example.onitama.api.ManejadorPartidaAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject

class ViewModelNotificaciones : ViewModel() {
    private val amigosApi = Amigos()
    private val manejadorPartidaAPI = ManejadorPartidaAPI()

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "tipo"
    }

    private val _notificaciones = MutableStateFlow<List<JSONObject>>(emptyList())
    val notificaciones = _notificaciones.asStateFlow()

    init {
        escucharNotificaciones()
    }

    /**
     * Escucha el flujo de mensajes entrantes del ManejadorGlobal
     * para detectar nuevas solicitudes de amistad enviadas por el servidor.
     */
    private fun escucharNotificaciones() {
        viewModelScope.launch {
            ManejadorGlobal.mensajesEntrantes.collectLatest { json ->
                val tipo = json.optString("tipo")
                val solicitud = json.optInt("idNotificacion", -1)

                when (tipo) {
                    "SOLICITUD_AMISTAD", "INVITACION_PARTIDA", "SOLICITAR_REANUDAR" -> {
                        try {
                            if (_notificaciones.value.none { it.optInt("idNotificacion") == solicitud }) {
                                _notificaciones.value = _notificaciones.value + json
                            }
                        } catch (e: Exception) {
                            Log.e("ViewModelNotif", "Error al decodificar solicitud de amistad", e)
                        }
                    }

                    "NOTIFICACION_CANCELADA" -> {
                        val borrarNotificacion = json.optInt("idNotificacion")
                       _notificaciones.value = _notificaciones.value.filter { it.optInt("idNotificacion") != borrarNotificacion }
                    }
                }
            }
        }
    }

    /**
     * Acepta una notificacion.
     */
    fun aceptar(solicitud: JSONObject, destinatario: String) {
        val idNotificacion = solicitud.optInt("idNotificacion")
        val tipo = solicitud.optString("tipo")
        val remitente = solicitud.optString("remitente")

        viewModelScope.launch {
            when(tipo) {
                "SOLICITUD_AMISTAD" ->
                    amigosApi.aceptarAmistad(remitente, destinatario)

                "INVITACION_PARTIDA" -> 
                    manejadorPartidaAPI.responderInvitacion(idNotificacion, true)

                "SOLICITAR_REANUDAR" ->
                    manejadorPartidaAPI.responderReanudacion(idNotificacion, destinatario, true)
            }
        }
        _notificaciones.value = _notificaciones.value.filter { it.optInt("idNotificacion") != idNotificacion }
    }

    /**
     * Rechaza una notificacion.
     */
    fun rechazar(solicitud: JSONObject, destinatario: String) {
        val idNotificacion = solicitud.optInt("idNotificacion")
        val tipo = solicitud.optString("tipo")

        viewModelScope.launch {
            when(tipo) {
                "SOLICITUD_AMISTAD" ->
                    amigosApi.rechazarAmistad(idNotificacion)

                "INVITACION_PARTIDA" -> 
                    manejadorPartidaAPI.responderInvitacion(idNotificacion, false)

                "SOLICITAR_REANUDAR" ->
                    manejadorPartidaAPI.responderReanudacion(idNotificacion, destinatario, false)
            }
        }
        _notificaciones.value = _notificaciones.value.filter { it.optInt("idNotificacion") != idNotificacion }
    }
}