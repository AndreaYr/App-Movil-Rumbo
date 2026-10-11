package com.app.rumbo.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.rumbo.R
import com.app.rumbo.core.theme.Sand
import com.app.rumbo.core.theme.Teal
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1500L

@Composable
fun SplashRoute(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onFinished()
    }

    SplashScreen()
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Sand),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Logo de Rumbo",
                tint = Color.Unspecified,
                modifier = Modifier.size(140.dp)
            )
            Text(
                text = "Rumbo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Teal
            )
            CircularProgressIndicator(
                color = Teal,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
