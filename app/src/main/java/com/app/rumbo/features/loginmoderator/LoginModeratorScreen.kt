package com.app.rumbo.features.loginmoderator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme

private val ModeratorBackground = Color(0xFF0F1E1B)
private val ModeratorInputBg = Color(0xFF162824)
private val ModeratorInputBorder = Color(0xFF2B443E)
private val ModeratorOrange = Color(0xFFF05A36)
private val ModeratorBadgeBorder = Color(0xFFC85B38)
private val ModeratorBadgeText = Color(0xFFC85B38)
private val ModeratorLabelText = Color(0xFF7DA89B)
private val ModeratorIconTint = Color(0xFF589382)
private val ModeratorLinkText = Color(0xFF589382)

@Composable
fun LoginModeratorRoute(
    onLoginSuccess: () -> Unit,
    onBackToUserLogin: () -> Unit,
    viewModel: LoginModeratorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    Log.d("RUMBO_NAV", "Composing LoginModeratorRoute status=${state.status}")

    LaunchedEffect(state.status) {
        when (state.status) {
            LoginModeratorStatus.SUCCESS -> {
                snackbarHostState.showSnackbar(state.message.orEmpty())
                onLoginSuccess()
            }
            LoginModeratorStatus.ERROR -> {
                snackbarHostState.showSnackbar(state.message.orEmpty())
                viewModel.resetResult()
            }
            LoginModeratorStatus.IDLE, LoginModeratorStatus.LOADING -> Unit
        }
    }

    LoginModeratorScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLogin = viewModel::login,
        onBackToUserLogin = onBackToUserLogin
    )
}

@Composable
fun LoginModeratorScreen(
    state: LoginModeratorUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
    onBackToUserLogin: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ModeratorBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo de Rumbo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(72.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Rumbo",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CONSOLA DE MODERACIÓN",
                        color = ModeratorIconTint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, ModeratorBadgeBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = ModeratorBadgeText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "ACCESO RESTRINGIDO",
                    color = ModeratorBadgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Ingresa con tu cuenta institucional",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Las cuentas de moderación requieren credenciales institucionales para acceder al panel de gestión.",
                color = ModeratorLabelText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Correo institucional",
                color = ModeratorLabelText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            ModeratorInput(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "correo@institucion.edu",
                icon = "✉",
                isError = state.emailError != null,
                keyboardType = KeyboardType.Email
            )
            state.emailError?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Contraseña",
                color = ModeratorLabelText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            ModeratorInput(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Ingresa tu contraseña",
                icon = "🔒",
                isError = state.passwordError != null,
                keyboardType = KeyboardType.Password,
                isPassword = !state.isPasswordVisible,
                trailingText = if (state.isPasswordVisible) "◉" else "◎",
                onTrailingClick = onTogglePasswordVisibility
            )
            state.passwordError?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onLogin,
                enabled = state.status != LoginModeratorStatus.LOADING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ModeratorOrange)
            ) {
                if (state.status == LoginModeratorStatus.LOADING) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Text("  Verificando...", color = Color.White)
                } else {
                    Text("Entrar al panel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onBackToUserLogin) {
                    Text(
                        text = "Volver al acceso de usuarios",
                        color = ModeratorLinkText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeratorInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: String,
    isError: Boolean,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    trailingText: String? = null,
    onTrailingClick: () -> Unit = {}
) {
    val borderColor = if (isError) Color(0xFFFF6B6B) else ModeratorInputBorder
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ModeratorInputBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = ModeratorIconTint, fontSize = 14.sp)
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
            cursorBrush = SolidColor(Color.White),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(placeholder, color = ModeratorLabelText, fontSize = 14.sp)
                    inner()
                }
            }
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                color = ModeratorIconTint,
                modifier = Modifier
                    .clickable { onTrailingClick() }
                    .padding(4.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1E1B)
@Composable
private fun LoginModeratorScreenPreview() {
    RumboTheme {
        LoginModeratorScreen(
            state = LoginModeratorUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLogin = {},
            onBackToUserLogin = {}
        )
    }
}
