package com.example.onitama.api

import org.json.JSONObject
import kotlin.apply
import com.example.onitama.api.ManejadorGlobal

class ManejadorPartidaAPI {
    fun enviarInvitacion(
        remitente: String,
        destinatario: String
    ) {
        val json = JSONObject().apply {
            put("tipo", "INVITACION_PARTIDA")
            put("remitente", remitente)
            put("destinatario", destinatario)
        }
        ManejadorGlobal.enviarMensaje(json.toString())
    }

    fun cancelarNotificacionEnviada(
        idNotificacion: Int
    ) {
        if (idNotificacion == -1) {
            return
        }

        val json = JSONObject().apply {
            put("tipo", "CANCELAR_NOTIFICACION")
            put("idNotificacion", idNotificacion)
        }
        ManejadorGlobal.enviarMensaje(json.toString())
    }

    fun responderInvitacion(
        idNotificacion: Int,
        aceptada: Boolean
    ) {
        var mensaje = ""
        if (aceptada) {
            mensaje = "ACEPTAR_INVITACION"
        }
        else {
            mensaje = "RECHAZAR_INVITACION"
        }

        val json = JSONObject().apply {
            put("tipo", mensaje)
            put("idNotificacion", idNotificacion)
        }
        ManejadorGlobal.enviarMensaje(json.toString())
    }

    fun obtenerPartidasPausadas(
        nombreUsuario: String
    ) {
        val json = JSONObject().apply {
            put("tipo", "SOLICITAR_PARTIDAS_PRIV")
            put("usuario", nombreUsuario)
        }
        ManejadorGlobal.enviarMensaje(json.toString())
    }

    fun solicitarReanudar(
        remitente: String,
        destinatario: String,
        idPartida: Int
    ) {
        val json = JSONObject().apply {
            put("tipo", "SOLICITAR_REANUDAR")
            put("remitente", remitente)
            put("destinatario", destinatario)
            put("idPartida", idPartida)
        }
        ManejadorGlobal.enviarMensaje(json.toString())
    }

    fun responderReanudacion(
        idNotificacion: Int,
        miNombre: String,
        aceptada: Boolean
    ){
        var mensaje = ""
        if (aceptada) {
            mensaje = "ACEPTAR_REANUDAR"
        }
        else {
            mensaje = "RECHAZAR_REANUDAR"
        }

        val json = JSONObject().apply {
            put("tipo", mensaje)
            put("idNotificacion", idNotificacion)
            put("nombre", miNombre)
        }
        ManejadorGlobal.enviarMensaje(json.toString())
    }
}