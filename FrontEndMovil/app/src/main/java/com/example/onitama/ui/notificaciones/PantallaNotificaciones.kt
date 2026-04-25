package com.example.onitama.ui.notificaciones

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onitama.AutoLogin
import com.example.onitama.R
import com.example.onitama.api.Amigos

/**
 * Pantalla que muestra las notificaciones.
 */
@Composable
fun PantallaNotificaciones(
    viewModel: ViewModelNotificaciones
) {
    val quattrocentoBold = FontFamily(Font(R.font.quattrocento_bold))
    val context = LocalContext.current
    val datosUsuario by AutoLogin.sesion.collectAsState()
    val listaNotif by viewModel.notificaciones.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ==========================================
        // 1. CABECERA
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(colorResource(id = R.color.azulFondo))
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = { /* Acción perfil */ },
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                // Imagen de perfil si se desea
            }

            Image(
                painter = painterResource(id = R.drawable.onitama_text),
                contentDescription = "Titulo",
                modifier = Modifier
                    .padding(start = 30.dp, top = 16.dp)
                    .height(60.dp)
                    .align(Alignment.TopStart)
            )
        }

        // ==========================================
        // 2. LISTA DE NOTIFICACIONES
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 130.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tus Notificaciones",
                fontFamily = quattrocentoBold,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (listaNotif.isEmpty()) {
                Text(
                    text = "No tienes notificaciones pendientes",
                    fontFamily = quattrocentoBold,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaNotif) { notif ->
                        NotificacionItem(
                            notif = notif,
                            fontFamily = quattrocentoBold,
                            onAceptar = { viewModel.aceptar(notif, datosUsuario?.nombre ?: "") },
                            onRechazar = { viewModel.rechazar(notif) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(
    notif: Amigos.MensajeSolicitudAmistadS,
    fontFamily: FontFamily,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = "${notif.remitente} te ha enviado una solicitud de amistad",
            fontFamily = fontFamily,
            fontSize = 16.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAceptar,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.azulFondo))
            ) {
                Text("Aceptar", color = Color.White)
            }
            Button(
                onClick = onRechazar,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
            ) {
                Text("Rechazar", color = Color.White)
            }
        }
    }
}