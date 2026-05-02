package com.example.onitama.ui.notificaciones

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
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
import com.example.onitama.ui.activities.MenuPrincipalActivity
import com.example.onitama.ui.activities.cartas.Cartas_activity
import com.example.onitama.ui.amigos.Amigos_Activity
import com.example.onitama.ui.perfil.Perfil_Activity

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
        // 1. CABECERA (Contadores y Perfil)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(colorResource(id = R.color.azulFondo))
                .padding(horizontal = 16.dp)
        ) {
            if(datosUsuario != null) {
                Log.d("DEBUG", "Imagen: ${datosUsuario?.avatar_id}")
                val imageResId = context.resources.getIdentifier(
                    datosUsuario?.avatar_id,
                    "drawable",
                    context.packageName
                )

                if (imageResId != 0) {
                    Image(
                        painter = painterResource(imageResId),
                        contentDescription = "Imagen de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterEnd)
                            .clip(CircleShape)
                            .clickable(onClick = {
                                val intent = Intent(context, Perfil_Activity::class.java)
                                context.startActivity(intent)
                            })
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = {
                                val intent = Intent(context, Perfil_Activity::class.java)
                                context.startActivity(intent)
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = datosUsuario?.nombre?.take(1)?.uppercase() ?: "",
                            color = colorResource(id = R.color.azulFondo),
                            fontSize = 32.sp,
                            fontFamily = quattrocentoBold
                        )
                    }
                }

                // B) Título del juego
                Image(
                    painter = painterResource(id = R.drawable.onitama_text),
                    contentDescription = "Titulo",
                    modifier = Modifier
                        .padding(start = 30.dp, top = 16.dp)
                        .height(60.dp)
                        .align(Alignment.TopStart)


                )

                // C) Contadores (Katanas y Core)
                Row(
                    modifier = Modifier
                        .padding(top = 30.dp, bottom = 10.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(id = R.drawable.katanas),
                            contentDescription = "Katanas",
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            datosUsuario?.puntos.toString(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontFamily = quattrocentoBold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(id = R.drawable.core),
                            contentDescription = "Core",
                            modifier = Modifier.height(30.dp)
                        )
                        Text(
                            datosUsuario?.cores.toString(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontFamily = quattrocentoBold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
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
            Spacer(modifier = Modifier.width(10.dp))

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

        // ==========================================
        // 3. BARRA INFERIOR DE TAREAS
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // Se ancla abajo del todo
        ) {
            // Fondo y botones laterales de la barra
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(63.dp)
                    .align(Alignment.BottomCenter)
                    .background(colorResource(id = R.color.azulBarraTareas)),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(60.dp)
                ) {
                    Image(
                        painterResource(R.drawable.tablero),
                        contentDescription = "Skins"
                    )
                }
                IconButton(
                    onClick = {
                        val intent = Intent(context, Cartas_activity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },

                    modifier = Modifier.size(60.dp)
                ) {
                    Image(
                        painterResource(R.drawable.cards),
                        contentDescription = "Cards"
                    )
                }

                Spacer(modifier = Modifier.width(80.dp)) // Hueco para el botón central

                IconButton(
                    onClick = {
                        val intent = Intent(context, Amigos_Activity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.size(60.dp)
                ) {
                    Image(
                        painterResource(R.drawable.amigos),
                        contentDescription = "Amigos"
                    )
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(60.dp)
                ) {
                    Image(
                        painterResource(R.drawable.carrito),
                        contentDescription = "Tienda"
                    )
                }
            }

            // Botón central "A JUGAR" sobresaliendo
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        val intent = Intent(context, MenuPrincipalActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.size(70.dp)
                ) {
                    Image(painterResource(R.drawable.espadas), contentDescription = "Jugar")
                }
                Text(
                    text = "¡A JUGAR!",
                    fontFamily = quattrocentoBold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.offset(y = (-8).dp)
                )
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