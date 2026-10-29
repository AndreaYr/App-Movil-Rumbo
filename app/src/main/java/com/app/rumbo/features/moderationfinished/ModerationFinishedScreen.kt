package com.app.rumbo.features.moderationfinished

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.rumbo.R

private val BodyBg = Color(0xFF0F1E1B)
private val White = Color(0xFFFFFFFF)
private val VerifyGreen = Color(0xFF117361)
private val TextSecondary = Color(0xFF7DA89B)

@Composable
fun ModerationFinishedScreen(
    state: ModerationFinishedUiState,
    onBackToPanel: () -> Unit = {}
) {
    val iconColor = when (state.outcome) {
        ModerationOutcome.APPROVED -> VerifyGreen
        ModerationOutcome.REJECTED -> Color(0xFFE76F51)
        ModerationOutcome.FINISHED -> TextSecondary
    }
    val iconRes = when (state.outcome) {
        ModerationOutcome.APPROVED -> R.drawable.ic_check_circle
        ModerationOutcome.REJECTED -> R.drawable.ic_cancel
        ModerationOutcome.FINISHED -> R.drawable.ic_task_alt
    }
    Scaffold(
        containerColor = BodyBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F3B35)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = iconColor, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(state.title, color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(state.message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBackToPanel,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = iconColor)
            ) {
                Text("Volver al panel", color = White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
