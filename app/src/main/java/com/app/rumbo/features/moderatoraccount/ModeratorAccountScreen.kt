package com.app.rumbo.features.moderatoraccount

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.rumbo.R

private val BodyBg = Color(0xFF0F1E1B)
private val White = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF7DA89B)
private val RejectOrange = Color(0xFFF05A36)
private val SurfaceBg = Color(0xFF162824)

@Composable
fun ModeratorAccountScreen(
    state: ModeratorAccountUiState,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
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
                Text("Cuenta", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F3B35)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(R.drawable.ic_person), contentDescription = null, tint = White, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(state.name, color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(state.email, color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(state.role, color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(32.dp))
            Surface(
                color = SurfaceBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.ic_shield), contentDescription = null, tint = TextSecondary)
                        Spacer(Modifier.width(12.dp))
                        Text("Acceso de moderación", color = White)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RejectOrange)
            ) {
                Icon(painter = painterResource(R.drawable.ic_cancel), contentDescription = null, tint = White)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", color = White, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
