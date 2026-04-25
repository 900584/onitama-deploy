package com.example.onitama.ui.activities.notificaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onitama.ui.login.ViewModelInicioSesion
import com.example.onitama.ui.notificaciones.PantallaNotificaciones
import com.example.onitama.ui.notificaciones.ViewModelNotificaciones

class NotificacionesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: ViewModelNotificaciones = viewModel()

            MaterialTheme {
                Surface {
                    PantallaNotificaciones(viewModel = viewModel)
                }
            }
        }
    }
}
