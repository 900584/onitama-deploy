package com.example.onitama.ui.perfil

import android.content.Intent
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.onitama.ui.activities.cartas.Cartas_activity
import com.example.onitama.ui.perfil.ViewModelPerfil
import com.example.onitama.ui.activities.notificaciones.NotificacionesActivity

/**
 * Pantalla que muestra datos del usuario.
 *
 * Esta función es un Composable que representa la pantalla que
 * muestra el perfil del usuario.
 *
 * @param viewModel View Model que gestiona el estado y la lógica.
 */
@Composable
fun PantallaPerfil(
    viewModel: ViewModelPerfil
) {

    val datosUsuario by AutoLogin.sesion.collectAsState()
    val context = LocalContext.current
    val quattrocentoBold = FontFamily(Font(R.font.quattrocento_bold))

    // Resolución de la imagen de perfil
    val imageResId = context.resources.getIdentifier(
        datosUsuario?.avatar_id,
        "drawable",
        context.packageName
    )
    val idSeguro = if (imageResId != 0) imageResId else R.drawable.rey_azul

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
            // A) Botón de Perfil
            IconButton(
                onClick = { /* Acción perfil */ },
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {

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

        // ==========================================
        // 2. DATOS DEL USUARIO
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp, bottom = 63.dp), // Espacio entre cabecera y pie
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagen de perfil
            Image(
                painter = painterResource(id = idSeguro),
                contentDescription = "Imagen de Perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón 'Editar Perfil'
                Button(
                    onClick = { /* Acción editar perfil */ },
                    modifier = Modifier.weight(1f),
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
                        val intent = Intent(context, NotificacionesActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
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
            }
        }

        // ==========================================
        // 3. BARRA INFERIOR DE TAREAS
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
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
                ){
                    Image(painterResource(R.drawable.tablero),
                        contentDescription = "Skins")
                }
                IconButton(
                    onClick = {
                        val intent = Intent(context, Cartas_activity::class.java)
                        context.startActivity(intent)},
                    modifier = Modifier.size(60.dp)
                ) {
                    Image(painterResource(R.drawable.cards),
                        contentDescription = "Cards")
                }

                Spacer(modifier = Modifier.width(80.dp)) // Hueco para el botón central

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(70.dp)
                ){
                    Image(painterResource(R.drawable.amigos),
                        contentDescription = "Amigos")
                }
                Text(
                    text = "AMIGOS",
                    fontFamily = quattrocentoBold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.offset(y = (-8).dp)
                )
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(60.dp)
                ) {
                    Image(
                        painterResource(R.drawable.carrito),
                        contentDescription = "Tienda")
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
                    onClick = { /* Iniciar partida rápida */ },
                    modifier = Modifier.size(60.dp)
                ) {
                    Image(painterResource(R.drawable.espadas), contentDescription = "Jugar")
                }
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