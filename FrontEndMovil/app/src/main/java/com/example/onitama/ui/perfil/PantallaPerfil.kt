package com.example.onitama.ui.perfil

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.onitama.AutoLogin.cerrarSesion
import com.example.onitama.R
import com.example.onitama.api.ManejadorGlobal
import com.example.onitama.ui.activities.Ini_Activity
import com.example.onitama.ui.activities.MenuPrincipalActivity
import com.example.onitama.ui.notificaciones.Notificaciones_Activity
import com.example.onitama.ui.activities.cartas.Cartas_activity
import com.example.onitama.ui.amigos.Amigos_Activity

/**
 * Pantalla que muestra datos del usuario.
 *
 * Esta función es un Composable que representa la pantalla que
 * muestra el perfil del usuario.
 *
 * @param viewModel View Model que gestiona el estado y la lógica.
 */
@Composable
fun PantallaPerfil() {

    val datosUsuario by AutoLogin.sesion.collectAsState()
    val context = LocalContext.current
    val quattrocentoBold = FontFamily(Font(R.font.quattrocento_bold))

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
            if (datosUsuario != null) {
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
        // 2. DATOS DEL USUARIO
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp, bottom = 63.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagen de perfil
            val imageResId = context.resources.getIdentifier(
                datosUsuario?.avatar_id,
                "drawable",
                context.packageName
            )

            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Imagen de Perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = datosUsuario?.nombre?.take(1)?.uppercase() ?: "",
                        color = colorResource(id = R.color.azulFondo),
                        fontSize = 64.sp,
                        fontFamily = quattrocentoBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Información del jugador
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Nombre de usuario:",
                    value = datosUsuario?.nombre ?: "Nombre de usuario",
                    fontFamily = quattrocentoBold
                )
                InfoRow(
                    label = "Correo electrónico:",
                    value = datosUsuario?.correo ?: "Correo electrónico",
                    fontFamily = quattrocentoBold
                )
                InfoRow(
                    label = "Partidas Jugadas:",
                    value = datosUsuario?.partidas_jugadas?.toString() ?: "0",
                    fontFamily = quattrocentoBold
                )
                InfoRow(
                    label = "Partidas Ganadas:",
                    value = datosUsuario?.partidas_ganadas?.toString() ?: "0",
                    fontFamily = quattrocentoBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón 'Editar Perfil'
                Button(
                    onClick = { /* Acción editar perfil */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.azulBarraTareas),
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.editar),
                        contentDescription = "Editar Perfil",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EDITAR",
                        fontFamily = quattrocentoBold,
                        fontSize = 12.sp
                    )
                }

                // Botón 'Notificaciones'
                Button(
                    onClick = {
                        val intent = Intent(context, Notificaciones_Activity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.azulBarraTareas),
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.notificacion),
                        contentDescription = "Notificaciones",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NOTIFICACIONES",
                        fontFamily = quattrocentoBold,
                        fontSize = 12.sp
                    )
                }

                // Botón 'Cerrar Sesión'
                Button(
                    onClick = {
                        cerrarSesion(context)
                        val intent = Intent(context, Ini_Activity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        ManejadorGlobal.desconectar()
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "CERRAR SESIÓN",
                        fontFamily = quattrocentoBold,
                        fontSize = 12.sp
                    )
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

/**
 * Fila que muestra una etiqueta y un valor de información del usuario.
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    fontFamily: FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = fontFamily,
            fontSize = 18.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontFamily = fontFamily,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}