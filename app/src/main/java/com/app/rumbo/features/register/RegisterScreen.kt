package com.app.rumbo.features.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.ErrorRed
import com.app.rumbo.core.theme.Outline
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.theme.Sand
import com.app.rumbo.core.theme.Teal
import com.app.rumbo.core.theme.White

@Composable
fun RegisterRoute(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status == RegisterStatus.SUCCESS) onRegisterSuccess()
    }

    RegisterScreen(
        state = state,
        onBackToLogin = onBackToLogin,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onTermsAcceptanceChange = viewModel::onTermsAcceptanceChange,
        onRegister = viewModel::register
    )
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onBackToLogin: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onTermsAcceptanceChange: (Boolean) -> Unit,
    onRegister: () -> Unit
) {
    Scaffold(containerColor = Sand) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(White)
                    .border(1.dp, Outline, RoundedCornerShape(10.dp))
                    .clickable(onClick = onBackToLogin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Volver a iniciar sesión",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("Crea tu cuenta", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "Únete a la comunidad que está mapeando el Quindío.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            RegisterFieldLabel("Nombre")
            RegisterInput(
                value = state.name,
                onValueChange = onNameChange,
                placeholder = "Tu nombre",
                leadingSymbol = "♙",
                error = state.nameError,
                showValidation = state.name.isNotBlank() || state.hasSubmitted
            )
            state.nameError?.let { RegisterErrorText(it) }
            Spacer(Modifier.height(8.dp))

            RegisterFieldLabel("Correo electrónico")
            RegisterInput(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "Correo electrónico",
                leadingSymbol = "✉",
                keyboardType = KeyboardType.Email,
                error = state.emailError,
                showValidation = state.email.isNotBlank() || state.hasSubmitted
            )
            state.emailError?.let { RegisterErrorText(it) }
            Spacer(Modifier.height(8.dp))

            RegisterFieldLabel("Contraseña")
            RegisterInput(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Contraseña",
                leadingSymbol = "♙",
                keyboardType = KeyboardType.Password,
                isPassword = !state.isPasswordVisible,
                trailingText = if (state.isPasswordVisible) "◉" else "◎",
                onTrailingClick = onTogglePasswordVisibility,
                error = state.passwordError,
                showValidation = false
            )
            PasswordSecurityIndicator(state)
            state.passwordError?.let { RegisterErrorText(it) }
            Spacer(Modifier.height(8.dp))

            RegisterFieldLabel("Confirmar contraseña")
            RegisterInput(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "Confirma tu contraseña",
                leadingSymbol = "♙",
                keyboardType = KeyboardType.Password,
                isPassword = !state.isConfirmPasswordVisible,
                trailingText = if (state.isConfirmPasswordVisible) "◉" else "◎",
                onTrailingClick = onToggleConfirmPasswordVisibility,
                error = state.confirmPasswordError,
                showValidation = state.confirmPassword.isNotBlank() || state.hasSubmitted
            )
            state.confirmPasswordError?.let { RegisterErrorText(it) }
            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = state.acceptedTerms,
                    onCheckedChange = onTermsAcceptanceChange,
                    modifier = Modifier.size(24.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = Teal,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    "Acepto los términos de uso y la política de tratamiento de datos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 4.dp, end = 4.dp)
                        .clickable { onTermsAcceptanceChange(!state.acceptedTerms) }
                )
            }
            state.termsError?.let { RegisterErrorText(it) }
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onRegister,
                enabled = state.status != RegisterStatus.LOADING,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                if (state.status == RegisterStatus.LOADING) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                    Text("  Creando cuenta…")
                } else {
                    Text("Crear cuenta", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onBackToLogin,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Inicia sesión", style = MaterialTheme.typography.labelMedium, color = Teal)
                }
            }
        }
    }
}

@Composable
private fun RegisterFieldLabel(label: String) {
    Text(label, style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(5.dp))
}

@Composable
private fun RegisterInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingSymbol: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    trailingText: String? = null,
    onTrailingClick: () -> Unit = {},
    error: String?,
    showValidation: Boolean
) {
    val hasError = error != null
    val borderColor = if (hasError) Coral else Outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (hasError) MaterialTheme.colorScheme.errorContainer else White)
            .border(1.dp, borderColor, RoundedCornerShape(13.dp))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(leadingSymbol, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(9.dp))
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
                    if (value.isEmpty()) {
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    inner()
                }
            }
        )
        if (trailingText != null) {
            IconButton(onClick = onTrailingClick, modifier = Modifier.size(30.dp)) {
                Text(trailingText, color = Teal, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (showValidation) {
            Icon(
                painter = painterResource(if (hasError) R.drawable.ic_cancel else R.drawable.ic_check_circle),
                contentDescription = if (hasError) "Campo inválido" else "Campo válido",
                tint = if (hasError) ErrorRed else Teal,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun PasswordSecurityIndicator(state: RegisterUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < state.passwordRuleCount) Teal else Outline)
            )
        }
    }
    val helperColor = when {
        state.passwordRuleCount == 3 -> Teal
        state.password.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> ErrorRed
    }
    Text(
        text = if (state.passwordRuleCount == 3) {
            "Segura · 10 caracteres, mayúscula y número"
        } else {
            "10 caracteres, mayúscula y número"
        },
        style = MaterialTheme.typography.bodySmall,
        color = helperColor,
        modifier = Modifier.padding(top = 3.dp)
    )
}

@Composable
private fun RegisterErrorText(message: String) {
    Text(message, style = MaterialTheme.typography.bodySmall, color = ErrorRed)
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun RegisterScreenPreview() {
    RumboTheme {
        RegisterScreen(
            state = RegisterUiState(),
            onBackToLogin = {}, onNameChange = {}, onEmailChange = {}, onPasswordChange = {},
            onConfirmPasswordChange = {}, onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {}, onTermsAcceptanceChange = {}, onRegister = {}
        )
    }
}
