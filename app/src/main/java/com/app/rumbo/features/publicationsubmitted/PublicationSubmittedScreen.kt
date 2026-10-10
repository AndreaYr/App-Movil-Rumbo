package com.app.rumbo.features.publicationsubmitted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.rumbo.R
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.Gold
import com.app.rumbo.core.theme.GoldContainer
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.theme.TealContainer
import com.app.rumbo.core.theme.TealDark

@Composable
fun PublicationSubmittedRoute(
    onMyPublications: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PublicationSubmittedViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
) {
    PublicationSubmittedScreen(
        publicationTitle = viewModel.title,
        onMyPublications = onMyPublications,
        onGoHome = onGoHome,
        modifier = modifier
    )
}

@Composable
fun PublicationSubmittedScreen(
    publicationTitle: String,
    onMyPublications: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Icono con fondo teal
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(TealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_schedule), contentDescription = null, tint = TealDark, modifier = Modifier.size(48.dp))
                }

                Text("¡Publicación enviada!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)

                Text(
                    "\"$publicationTitle\" está en la cola de revisión. Te avisamos cuando un moderador la verifique.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Recuadro pendiente de verificación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(GoldContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.ic_schedule), contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                    Text(
                        "PENDIENTE DE VERIFICACIÓN",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF8A6A0B)
                    )
                }

                // Recuadro insignia + puntos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color(0xFFE8F5F1))
                        .border(1.dp, TealContainer, MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.ic_award), contentDescription = null, tint = TealDark, modifier = Modifier.size(22.dp))
                    val text = buildAnnotatedString {
                        append("+25 puntos · ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Insignia \"Primera publicación\" desbloqueada") }
                    }
                    Text(text, style = MaterialTheme.typography.bodyMedium, color = TealDark)
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onMyPublications,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = Color.White)
                    ) {
                        Text("Ver mis publicaciones", style = MaterialTheme.typography.titleSmall)
                    }
                    Button(
                        onClick = onGoHome,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Volver al inicio", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PublicationSubmittedScreenPreview() {
    RumboTheme {
        PublicationSubmittedScreen(
            publicationTitle = "Mirador de la Vereda Cruces",
            onMyPublications = {},
            onGoHome = {}
        )
    }
}
