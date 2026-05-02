package com.example.onitama.ui.notificaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onitama.api.Amigos
import com.example.onitama.api.ManejadorGlobal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ViewModelNotificaciones : ViewModel() {
    private val amigosApi = Amigos()
    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "tipo"
    }

    private val _notificaciones = MutableStateFlow<List<Amigos.MensajeSolicitudAmistadS>>(emptyList())
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
                if (tipo == "SOLICITUD_AMISTAD") {
                    try {
                        val solicitud = jsonSerializer.decodeFromString<Amigos.MensajeSolicitudAmistadS>(json.toString())
                        if (_notificaciones.value.none { it.idNotificacion == solicitud.idNotificacion }) {
                            _notificaciones.value = _notificaciones.value + solicitud
                        }
                    } catch (e: Exception) {
                        Log.e("ViewModelNotif", "Error al decodificar solicitud de amistad", e)
                    }
                }
            }
        }
    }

    /**
     * Acepta una solicitud de amistad.
     */
    fun aceptar(solicitud: Amigos.MensajeSolicitudAmistadS, destinatario: String) {
        viewModelScope.launch {
            amigosApi.aceptarAmistad(solicitud.remitente, destinatario)
        }
    }

    /**
     * Rechaza una solicitud de amistad.
     */
    fun rechazar(solicitud: Amigos.MensajeSolicitudAmistadS) {
        viewModelScope.launch {
            amigosApi.rechazarAmistad(solicitud.idNotificacion)
        }
    }
}