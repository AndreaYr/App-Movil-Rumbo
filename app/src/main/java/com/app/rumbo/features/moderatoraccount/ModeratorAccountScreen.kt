package com.app.rumbo.features.moderatoraccount

import com.app.rumbo.core.theme.adaptiveColor
import com.app.rumbo.features.settings.SettingsSheet
import com.app.rumbo.core.component.LanguageDialog
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.rumbo.R

private val BodyBg: Color @Composable get() = adaptiveColor(Color(0xFFF7F5EE), Color(0xFF0F1E1B))
private val OnAccent = Color(0xFFFFFFFF)
private val TextMain: Color @Composable get() = adaptiveColor(Color(0xFF0F1E1B), Color(0xFFFFFFFF))
private val TextSecondary: Color @Composable get() = adaptiveColor(Color(0xFF5B6B65), Color(0xFF7DA89B))
private val RejectOrange: Color @Composable get() = adaptiveColor(Color(0xFFC4401E), Color(0xFFF05A36))
private val SurfaceBg: Color @Composable get() = adaptiveColor(Color(0xFFFFFFFF), Color(0xFF162824))
@Composable
fun ModeratorAccountScreen(
    state: ModeratorAccountUiState,
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showAccessibility by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    if (showAccessibility) SettingsSheet(onDismiss = { showAccessibility = false }, showLanguage = false)
    if (showLanguage) LanguageDialog(onDismiss = { showLanguage = false })

    Scaffold(
        containerColor = BodyBg,
        topBar = {
            // Respeta la barra de estado: sin este margen el botón de volver quedaba debajo y no recibía los toques
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.common_back), tint = TextMain)
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.moderator_account_title), color = TextMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    .background(adaptiveColor(Color(0xFFD7E6E1), Color(0xFF1F3B35))),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(R.drawable.ic_person), contentDescription = null, tint = TextMain, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(state.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(state.email, color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.moderator_role_label), color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(24.dp))
            Surface(
                color = SurfaceBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.ic_shield), contentDescription = null, tint = TextSecondary)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.moderator_account_access), color = TextMain)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            AccountOption(R.drawable.ic_edit, stringResource(R.string.moderator_account_edit_profile), onEditProfile)
            Spacer(Modifier.height(8.dp))
            AccountOption(R.drawable.ic_settings, stringResource(R.string.moderator_account_accessibility)) { showAccessibility = true }
            Spacer(Modifier.height(8.dp))
            AccountOption(R.drawable.ic_map, stringResource(R.string.moderator_account_language)) { showLanguage = true }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RejectOrange)
            ) {
                Icon(painter = painterResource(R.drawable.ic_cancel), contentDescription = null, tint = OnAccent)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.moderator_account_logout), color = OnAccent, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Fila de opción de la cuenta: icono, texto y flecha; toda la fila responde al toque. */
@Composable
private fun AccountOption(@DrawableRes icon: Int, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = SurfaceBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextMain, modifier = Modifier.weight(1f))
            Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}
