package com.app.rumbo.features.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.domain.model.MAX_BIO_LENGTH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private val DangerText = Color(0xFF8E2F13)
private val DangerBody = Color(0xFF7A4433)
private val DangerBorder = Color(0xFFC2431E)
private val DangerCardBorder = Color(0xFFF0C4B6)
private val ReadOnlyBackground = Color(0xFFF4F0E7)
private val HintColor = Color(0xFF8A9490)

@Composable
fun EditProfileRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EditProfileScreen(
        state = state,
        events = viewModel.events,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onBioChange = viewModel::onBioChange,
        onCityChange = viewModel::onCityChange,
        onSave = viewModel::onSave,
        onDeleteAccountClick = viewModel::onDeleteAccountClick,
        onDeleteDialogDismiss = viewModel::onDeleteDialogDismiss,
        onDeleteAccountConfirm = viewModel::onDeleteAccountConfirm,
        modifier = modifier
    )
}

@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    events: Flow<EditProfileEvent>,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onSave: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onDeleteDialogDismiss: () -> Unit,
    onDeleteAccountConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                EditProfileEvent.Saved -> onBack()
            }
        }
    }

    Column(modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        TopRow(onBack, Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp))
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AvatarSection(state.initials, Modifier.align(Alignment.CenterHorizontally).padding(top = 22.dp))
            Column(Modifier.padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FormField("Nombre") {
                    ProfileTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        error = state.nameError,
                        singleLine = true,
                        capitalization = KeyboardCapitalization.Words
                    )
                }
                FormField("Correo electrónico") {
                    ReadOnlyEmail(state.email)
                    Text(
                        "Para cambiarlo debes verificar el correo nuevo.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                        color = HintColor
                    )
                }
                FormField("Sobre mí") {
                    ProfileTextField(
                        value = state.bio,
                        onValueChange = onBioChange,
                        singleLine = false,
                        minLines = 3,
                        counter = "${state.bioLength} / $MAX_BIO_LENGTH",
                        capitalization = KeyboardCapitalization.Sentences
                    )
                }
                FormField("Ciudad base") {
                    ProfileTextField(
                        value = state.city,
                        onValueChange = onCityChange,
                        error = state.cityError,
                        singleLine = true,
                        leadingIcon = R.drawable.ic_near_me,
                        capitalization = KeyboardCapitalization.Words
                    )
                }
            }
            SaveButton(state.isSaving, onSave, Modifier.padding(top = 20.dp))
            SensitiveZone(onDeleteAccountClick, Modifier.padding(top = 24.dp, bottom = 24.dp))
        }
    }

    if (state.showDeleteDialog) {
        DeleteAccountDialog(onConfirm = onDeleteAccountConfirm, onDismiss = onDeleteDialogDismiss)
    }
}

@Composable
private fun TopRow(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .clickable(role = Role.Button, onClickLabel = "Volver", onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_arrow_back), null, Modifier.size(20.dp))
        }
        Text("Editar perfil", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
    }
}

/** La foto de perfil todavía no se puede cambiar: se muestra el diseño con las iniciales. */
@Composable
private fun AvatarSection(initials: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                    .border(2.5.dp, MaterialTheme.colorScheme.background, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_camera), null, Modifier.size(17.dp), tint = Color.White)
            }
        }
        Text(
            "Cambiar fotografía",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp), color = Color(0xFF4A5652))
        content()
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    capitalization: KeyboardCapitalization,
    modifier: Modifier = Modifier,
    error: String? = null,
    minLines: Int = 1,
    counter: String? = null,
    leadingIcon: Int? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = minLines,
        isError = error != null,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        leadingIcon = leadingIcon?.let {
            { Icon(painterResource(it), null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        supportingText = if (error != null || counter != null) {
            {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    if (counter != null) {
                        Text(counter, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun ReadOnlyEmail(email: String) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ReadOnlyBackground, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            email,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(painterResource(R.drawable.ic_lock), null, Modifier.size(17.dp), tint = HintColor)
    }
}

@Composable
private fun SaveButton(isSaving: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        if (isSaving) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text("Guardar cambios", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun SensitiveZone(onDeleteClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, shape)
            .border(1.dp, DangerCardBorder, shape)
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(R.drawable.ic_warning), null, Modifier.size(18.dp), tint = DangerText)
            Text("Zona sensible", style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp), color = DangerText)
        }
        Text(
            "Al eliminar tu cuenta se borran tus rutas y tu progreso. Tus publicaciones verificadas permanecen en el mapa de forma anónima.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 17.sp),
            color = DangerBody,
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedButton(
            onClick = onDeleteClick,
            shape = RoundedCornerShape(13.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerBorder),
            modifier = Modifier.padding(top = 13.dp).fillMaxWidth().height(46.dp)
        ) {
            Icon(painterResource(R.drawable.ic_delete_forever), null, Modifier.size(17.dp))
            Text("Eliminar mi cuenta", style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp), modifier = Modifier.padding(start = 7.dp))
        }
    }
}

@Composable
private fun DeleteAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar tu cuenta?", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                "Se borrarán tus rutas y tu progreso. Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Eliminar", color = DangerBorder) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun EditProfileScreenPreview() {
    RumboTheme {
        EditProfileScreen(
            state = EditProfileUiState(
                isLoading = false,
                initials = "MO",
                email = "mariana.ospina@correo.co",
                name = "Mariana Ospina",
                bio = "Caminante de fin de semana. Busco miradores y cafés de finca en el Quindío.",
                city = "Armenia, Quindío"
            ),
            events = emptyFlow(),
            onBack = {},
            onNameChange = {},
            onBioChange = {},
            onCityChange = {},
            onSave = {},
            onDeleteAccountClick = {},
            onDeleteDialogDismiss = {},
            onDeleteAccountConfirm = {}
        )
    }
}
