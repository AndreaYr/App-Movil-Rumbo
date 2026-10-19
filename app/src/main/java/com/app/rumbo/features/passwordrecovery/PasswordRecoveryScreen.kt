package com.app.rumbo.features.passwordrecovery

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.ErrorContainer
import com.app.rumbo.core.theme.ErrorRed
import com.app.rumbo.core.theme.Outline
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.theme.Sand
import com.app.rumbo.core.theme.Teal
import com.app.rumbo.core.theme.TealContainer
import com.app.rumbo.core.theme.White

@Composable
fun PasswordRecoveryRoute(
    onBack: () -> Unit,
    viewModel: PasswordRecoveryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.status, state.message) {
        if (state.message != null) {
            snackbarHostState.showSnackbar(state.message.orEmpty())
            viewModel.clearMessage()
        }
    }

    PasswordRecoveryScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEmailChange = viewModel::onEmailChange,
        onSendLink = viewModel::sendLink,
        onShowErrorDemo = viewModel::showErrorDemo
    )
}

@Composable
fun PasswordRecoveryScreen(
    state: PasswordRecoveryUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSendLink: () -> Unit,
    onShowErrorDemo: () -> Unit
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(White)
                    .border(1.dp, Outline, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Recupera tu acceso", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Escribe el correo con el que te registraste y te enviamos un enlace para crear una contraseña nueva.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            Text("Correo electrónico", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(5.dp))
            RecoveryEmailField(
                email = state.email,
                error = state.emailError,
                onEmailChange = onEmailChange
            )
            state.emailError?.let { RecoveryErrorText(it) }
            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onSendLink,
                enabled = state.status != PasswordRecoveryStatus.LOADING,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                if (state.status == PasswordRecoveryStatus.LOADING) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
                    Text("  Enviando…", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("▷", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar enlace", style = MaterialTheme.typography.labelLarge)
                }
            }

            state.message?.let { message ->
                RecoveryStatusMessage(
                    message = message,
                    isError = state.status == PasswordRecoveryStatus.ERROR
                )
            }

            TextButton(
                onClick = onShowErrorDemo,
                enabled = state.status != PasswordRecoveryStatus.LOADING,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Ver estado de error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecoveryEmailField(
    email: String,
    error: String?,
    onEmailChange: (String) -> Unit
) {
    val borderColor = if (error != null) ErrorRed else Outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (error != null) ErrorContainer else White)
            .border(1.dp, borderColor, RoundedCornerShape(13.dp))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✉", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(9.dp))
        BasicTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(Teal),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            decorationBox = { inner ->
                Box {
                    if (email.isEmpty()) {
                        Text("Correo electrónico", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun RecoveryErrorText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = ErrorRed,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun RecoveryStatusMessage(message: String, isError: Boolean) {
    val containerColor = if (isError) ErrorContainer else TealContainer
    val contentColor = if (isError) ErrorRed else Teal
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = contentColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    )
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PasswordRecoveryScreenPreview() {
    RumboTheme {
        PasswordRecoveryScreen(
            state = PasswordRecoveryUiState(),
            onBack = {}, onEmailChange = {}, onSendLink = {}, onShowErrorDemo = {}
        )
    }
}
