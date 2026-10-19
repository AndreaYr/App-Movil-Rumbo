package com.app.rumbo.features.onboarding

import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.theme.Sand
import com.app.rumbo.core.theme.Teal
import com.app.rumbo.core.theme.White
import kotlinx.coroutines.delay

@Composable
fun OnboardingRoute(
    onContinueClick: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Log.d("RUMBO_NAV", "Composing OnboardingRoute")
    var hasNavigated by remember { mutableStateOf(false) }
    val currentOnContinue by rememberUpdatedState(onContinueClick)

    val navigateToLogin = remember {
        {
            if (!hasNavigated) {
                hasNavigated = true
                currentOnContinue()
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(2000L)
        navigateToLogin()
    }

    OnboardingScreen(onContinueClick = navigateToLogin)
}

@Composable
fun OnboardingScreen(
    onContinueClick: () -> Unit
) {
    Scaffold(containerColor = Sand) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Teal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo de Rumbo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(140.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Bienvenido a Rumbo",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Descubre y comparte los mejores lugares del Quindío. Crea tu comunidad de viajeros.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Indicador de carga para la pantalla de carga (Splash)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Teal,
                    strokeWidth = 2.5.dp
                )
                Text(
                    text = "Cargando…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OnboardingFeature(
                    icon = "🗺",
                    title = "Explora lugares",
                    description = "Descubre nuevos destinos y rutas en el Quindío"
                )

                OnboardingFeature(
                    icon = "👥",
                    title = "Comparte experiencias",
                    description = "Conecta con otros viajeros y comparte tus favoritos"
                )

                OnboardingFeature(
                    icon = "💬",
                    title = "Deja comentarios",
                    description = "Opina y ayuda a la comunidad con tus reseñas"
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.labelLarge,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnboardingFeature(
    icon: String,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$icon $title",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    RumboTheme {
        OnboardingScreen(onContinueClick = {})
    }
}
