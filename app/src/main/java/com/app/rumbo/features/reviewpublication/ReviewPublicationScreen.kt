package com.app.rumbo.features.reviewpublication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.rumbo.R

private val BodyBg = Color(0xFF0F1E1B)
private val SurfaceBg = Color(0xFF162824)
private val White = Color(0xFFFFFFFF)
private val BorderColor = Color(0xFF2B443E)
private val PendingBadgeBg = Color(0xFFFEF3C7)
private val PendingBadgeText = Color(0xFF9A7B1C)
private val VerifyGreen = Color(0xFF117361)
private val RejectOrange = Color(0xFFF05A36)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF7DA89B)
private val TextTertiary = Color(0xFF94A3B8)

@Composable
fun ReviewPublicationRoute(
    onBack: () -> Unit = {},
    onModerationCompleted: () -> Unit = {},
    onReject: (String) -> Unit = {},
    viewModel: ReviewPublicationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.moderationCompleted) {
        if (state.moderationCompleted) onModerationCompleted()
    }
    ReviewPublicationScreen(
        state = state,
        onBack = onBack,
        onVerify = viewModel::approve,
        onReject = { publicationId -> onReject(publicationId) }
    )
}

@Composable
fun ReviewPublicationScreen(
    state: ReviewPublicationUiState,
    onBack: () -> Unit = {},
    onVerify: () -> Unit = {},
    onReject: (String) -> Unit = {}
) {
    val publication = state.publication
    Scaffold(
        containerColor = BodyBg,
        bottomBar = {
            Surface(color = BodyBg) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { publication?.let { onReject(it.id) } },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RejectOrange)
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_cancel), contentDescription = null, tint = RejectOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rechazar", color = RejectOrange, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onVerify,
                        enabled = !state.isProcessing && !state.moderationCompleted,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerifyGreen)
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_check_circle), contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Verificar", color = White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.actionFailed) {
                Text(
                    text = stringResource(R.string.moderation_action_failed),
                    color = RejectOrange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Atrás", tint = White)
                }
                Spacer(Modifier.weight(1f))
                Text("foto 1/3 · mirador sobre el cañón", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(SurfaceBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(PendingBadgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("PENDIENTE · REVISIÓN 3/8", color = PendingBadgeText, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
                Text(publication?.title ?: "Mirador de la Vereda Cruces", color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F3B35)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(publication?.author?.initials ?: "MO", color = White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(publication?.author?.name ?: "Mariana Ospina", color = White, fontWeight = FontWeight.SemiBold)
                        Text("${publication?.author?.levelName ?: "Exploradora"} · 6 publicaciones verificadas · 0 rechazos", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                ReviewSection("CATEGORÍA DECLARADA", R.drawable.ic_category_nature, "${publication?.category?.name ?: "Naturaleza"} · miradores y senderos")
                ReviewSection("DESCRIPCIÓN", R.drawable.ic_edit, publication?.description ?: "Mirador poco conocido sobre el cañón, a 15 minutos del casco urbano. Hay una caseta de café y bancas de guadua.")
                ReviewSection("HORARIO SUGERIDO", R.drawable.ic_schedule, publication?.schedule ?: "7:00 – 18:00 · todos los días")
                ReviewSection("RANGO DE PRECIO", R.drawable.ic_money, "Económico · entrada $5.000")
                ReviewSection("UBICACIÓN", R.drawable.ic_place, "${publication?.town ?: "Vereda Cruces"}, Salento · 14,2 km del centro")
                ReviewSection("Ubicación declarada", R.drawable.ic_map, "${publication?.latitude ?: 4.5871}, ${publication?.longitude ?: -75.6420}")
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceBg)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_near_me), contentDescription = null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text("Verificar en Street View", color = White)
                }
            }
        }
    }
}

@Composable
private fun ReviewSection(title: String, iconRes: Int = 0, content: String) {
    Column {
        Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Top) {
            if (iconRes != 0) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(content, color = White, fontSize = 14.sp)
        }
    }
}
