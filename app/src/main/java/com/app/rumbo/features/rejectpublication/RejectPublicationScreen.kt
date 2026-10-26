package com.app.rumbo.features.rejectpublication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R

private val BodyBg = Color(0xFF0F1E1B)
private val SurfaceBg = Color(0xFF162824)
private val White = Color(0xFFFFFFFF)
private val BorderColor = Color(0xFF2B443E)
private val RejectOrange = Color(0xFFF05A36)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF7DA89B)
private val ErrorRed = Color(0xFFE76F51)

@Composable
fun RejectPublicationRoute(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onModerationCompleted: (String) -> Unit = {},
    viewModel: RejectPublicationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.moderationCompleted, state.completedReason) {
        if (state.moderationCompleted) state.completedReason?.let(onModerationCompleted)
    }
    RejectPublicationScreen(
        state = state,
        onBack = onBack,
        onCancel = onCancel,
        onConfirmReject = viewModel::confirmReject,
        onReasonSelected = viewModel::selectReason,
        onDetailChanged = viewModel::updateDetail
    )
}

@Composable
fun RejectPublicationScreen(
    state: RejectPublicationUiState,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onConfirmReject: () -> Unit = {},
    onReasonSelected: (String) -> Unit = {},
    onDetailChanged: (String) -> Unit = {}
) {
    val publication = state.publication
    Scaffold(
        containerColor = BodyBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Atrás", tint = White)
                }
                Spacer(Modifier.width(8.dp))
                Text("Rechazar publicación", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.imePadding(),
                color = BodyBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(0.85f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceBg)
                    ) {
                        Text("Cancelar", color = White)
                    }
                    Button(
                        onClick = onConfirmReject,
                        enabled = !state.isProcessing && !state.moderationCompleted,
                        modifier = Modifier.weight(1.35f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RejectOrange)
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_cancel), contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Rechazar publicación",
                            color = White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
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
                .padding(horizontal = 16.dp)
        ) {
            if (state.actionFailed) {
                Text(
                    text = stringResource(R.string.moderation_action_failed),
                    color = ErrorRed,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1F3B35)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(R.drawable.ic_place), contentDescription = null, tint = TextSecondary)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(publication?.title ?: "Mirador de la Vereda Cruces", color = White, fontWeight = FontWeight.SemiBold)
                    Text("${publication?.category?.name ?: "Naturaleza"} · ${publication?.author?.name ?: "Mariana Ospina"} · hace 2 d", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(painter = painterResource(R.drawable.ic_warning), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("El motivo es obligatorio y se le muestra al usuario. La publicación rechazada no aparecerá en el feed público.", color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("Motivo del rechazo *", color = White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            val reasons = listOf("Información insuficiente", "Ubicación incorrecta", "Fotografías no válidas", "Punto de interés no encontrado", "Información inconsistente")
            reasons.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onReasonSelected(reason) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = reason == state.selectedReason,
                        onClick = { onReasonSelected(reason) },
                        colors = RadioButtonDefaults.colors(selectedColor = RejectOrange, unselectedColor = TextSecondary)
                    )
                    Text(reason, color = White)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Detalle para el usuario", color = White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = state.detail,
                    onValueChange = onDetailChanged,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 13.sp),
                    maxLines = 4
                )
            }
            Text("${state.charCount} / 300", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
            Spacer(Modifier.height(16.dp))
        }
    }
}
