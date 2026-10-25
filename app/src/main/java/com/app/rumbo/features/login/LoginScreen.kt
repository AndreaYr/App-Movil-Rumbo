package com.app.rumbo.features.login

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.theme.Sand
import com.app.rumbo.core.theme.Teal
import com.app.rumbo.core.theme.White

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onRecoveryClick: () -> Unit,
    onModeratorClick: () -> Unit = {},
    registrationMessage: String? = null,
    onRegistrationMessageConsumed: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    Log.d("RUMBO_NAV", "Composing LoginRoute status=${state.status}")

    LaunchedEffect(state.status) {
        when (state.status) {
            LoginStatus.SUCCESS -> {
                snackbarHostState.showSnackbar(state.message.orEmpty())
                onLoginSuccess()
            }
            LoginStatus.ERROR -> {
                snackbarHostState.showSnackbar(state.message.orEmpty())
                viewModel.resetResult()
            }
            LoginStatus.IDLE, LoginStatus.LOADING -> Unit
        }
    }

    LaunchedEffect(registrationMessage) {
        if (registrationMessage != null) {
            snackbarHostState.showSnackbar(registrationMessage)
            onRegistrationMessageConsumed()
        }
    }

    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLogin = viewModel::login,
        onRegisterClick = onRegisterClick,
        onRecoveryClick = onRecoveryClick,
        onModeratorClick = onModeratorClick
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
    onRegisterClick: () -> Unit,
    onRecoveryClick: () -> Unit,
    onModeratorClick: () -> Unit = {}
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Sand
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Teal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo de Rumbo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(108.dp)
                    )
                }
                Text(
                    text = "Rumbo",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("Bienvenido de vuelta", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Tus lugares guardados y tus rutas te esperan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))

            Text("Correo electrónico", style = MaterialTheme.typography.labelMedium)
            LoginInput(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "Correo electrónico",
                icon = "✉",
                isError = state.emailError != null,
                keyboardType = KeyboardType.Email
            )
            state.emailError?.let { ErrorText(it) }

            Text("Contraseña", style = MaterialTheme.typography.labelMedium)
            LoginInput(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Contraseña",
                icon = "🔒",
                isError = state.passwordError != null,
                keyboardType = KeyboardType.Password,
                isPassword = !state.isPasswordVisible,
                trailingText = if (state.isPasswordVisible) "◉" else "◎",
                onTrailingClick = onTogglePasswordVisibility
            )
            state.passwordError?.let { ErrorText(it) }

            TextButton(
                onClick = onRecoveryClick,
                modifier = Modifier.align(Alignment.End).height(36.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)
            ) {
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.labelMedium, color = Teal)
            }

            Button(
                onClick = onLogin,
                enabled = state.status != LoginStatus.LOADING,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                if (state.status == LoginStatus.LOADING) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                    Text("  Iniciando sesión…")
                } else {
                    Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                Text("O CONTINÚA COMO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleButton("◉", "Invitado", Modifier.weight(1f), selected = false, onClick = {})
                RoleButton("♙", "Moderador", Modifier.weight(1f), selected = true, onClick = onModeratorClick)
            }
            Spacer(Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("¿Primera vez? ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Regístrate", style = MaterialTheme.typography.labelMedium, color = Teal)
                }
            }
        }
    }
}

@Composable
private fun LoginInput(
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
    val borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(White)
            .border(1.dp, borderColor, RoundedCornerShape(13.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = Teal, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(Teal),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    inner()
                }
            }
        )
        if (trailingText != null) {
            TextButton(onClick = onTrailingClick, modifier = Modifier.size(32.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                Text(trailingText, color = Teal)
            }
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun RoleButton(icon: String, label: String, modifier: Modifier, selected: Boolean, onClick: () -> Unit = {}) {
    val background = if (selected) MaterialTheme.colorScheme.onSurface else White
    val foreground = if (selected) White else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(icon, color = foreground)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = foreground)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    RumboTheme {
        LoginScreen(
            state = LoginUiState(),
            onEmailChange = {}, onPasswordChange = {}, onTogglePasswordVisibility = {}, onLogin = {},
            onRegisterClick = {}, onRecoveryClick = {}
        )
    }
}
