package com.app.rumbo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RumboTheme {
                // Cada pantalla gestiona sus márgenes del sistema (barra de estado y navegación)
                AppNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
