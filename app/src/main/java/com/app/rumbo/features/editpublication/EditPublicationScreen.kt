package com.app.rumbo.features.editpublication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.toTimeAgo
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.features.createpublication.CategoryPicker
import com.app.rumbo.features.createpublication.DescriptionField
import com.app.rumbo.features.createpublication.FormField
import com.app.rumbo.features.createpublication.LocationMap
import com.app.rumbo.features.createpublication.PriceField
import com.app.rumbo.features.createpublication.RumboTextField

private val RejectedBackground = Color(0xFFFCE8E2)
private val RejectedBorder = Color(0xFFF0C4B6)
private val RejectedContent = Color(0xFF8E2F13)

@Composable
fun EditPublicationRoute(
    onClose: () -> Unit,
    onResubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPublicationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EditPublicationScreen(
        state = state,
        onClose = onClose,
        onTitleChange = viewModel::onTitleChange,
        onCategorySelected = viewModel::onCategorySelected,
        onDescriptionChange = viewModel::onDescriptionChange,
        onScheduleChange = viewModel::onScheduleChange,
        onPriceSelected = viewModel::onPriceSelected,
        onMarkerMoved = viewModel::onMarkerMoved,
        onResubmit = { viewModel.onResubmit(onResubmitted) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublicationScreen(
    state: EditPublicationUiState,
    onClose: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onScheduleChange: (String) -> Unit,
    onPriceSelected: (PriceLevel) -> Unit,
    onMarkerMoved: (Double, Double) -> Unit,
    onResubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Editar publicación", style = MaterialTheme.typography.titleLarge)
                    if (!state.isLoading) {
                        Text(
                            "Creada ${state.createdAt.toTimeAgo()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Atrás")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        if (state.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RejectionNotice(state.rejectionReason, state.rejectionDetail)

            FormField("Título") {
                RumboTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    placeholder = "Mirador de la Vereda Cruces",
                    isError = state.titleError != null,
                    supportingText = state.titleError
                )
            }
            FormField("Categoría") {
                CategoryPicker(selected = state.category, onSelected = onCategorySelected)
            }
            FormField("Descripción") {
                DescriptionField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    error = state.descriptionError
                )
            }
            FormField("Horario sugerido") {
                RumboTextField(
                    value = state.schedule,
                    onValueChange = onScheduleChange,
                    placeholder = "7:00 - 18:00",
                    leadingIcon = R.drawable.ic_schedule
                )
            }
            FormField("Rango de precio") {
                PriceField(price = state.price, onSelected = onPriceSelected)
            }
            FormField("Ubicación · arrastra el marcador") {
                LocationMap(
                    latitude = state.latitude,
                    longitude = state.longitude,
                    markerColor = Coral,
                    onMarkerMoved = onMarkerMoved,
                    modifier = Modifier.fillMaxWidth().height(190.dp)
                )
            }
        }

        ActionBar(isSaving = state.isSaving, onCancel = onClose, onResubmit = onResubmit)
    }
}

@Composable
private fun RejectionNotice(reason: String?, detail: String?) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RejectedBackground, shape)
            .border(1.dp, RejectedBorder, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painterResource(R.drawable.ic_cancel), null, Modifier.size(16.dp), tint = RejectedContent)
            Text(
                "ESTADO ACTUAL: RECHAZADA",
                style = MaterialTheme.typography.labelMedium,
                color = RejectedContent
            )
        }
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Motivo del moderador: ") }
                append(reason ?: "Sin motivo registrado")
                if (!detail.isNullOrBlank()) append(" — $detail")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = RejectedContent
        )
    }
}

@Composable
private fun ActionBar(isSaving: Boolean, onCancel: () -> Unit, onResubmit: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancelar", style = MaterialTheme.typography.titleSmall)
            }
            Button(
                onClick = onResubmit,
                enabled = !isSaving,
                modifier = Modifier.weight(1.6f).height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral,
                    contentColor = Color.White,
                    disabledContainerColor = Coral.copy(alpha = 0.6f),
                    disabledContentColor = Color.White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(painterResource(R.drawable.ic_send), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        "Reenviar a revisión",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun EditPublicationScreenPreview() {
    RumboTheme {
        EditPublicationScreen(
            state = EditPublicationUiState(
                isLoading = false,
                createdAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L,
                rejectionReason = "Ubicación incorrecta",
                rejectionDetail = "El marcador quedó sobre la vía, no sobre el mirador.",
                title = "Mirador de la Vereda Cruces",
                description = "Mirador poco conocido sobre el cañón.",
                schedule = "7:00 - 18:00",
                latitude = 4.5883,
                longitude = -75.6411
            ),
            onClose = {},
            onTitleChange = {},
            onCategorySelected = {},
            onDescriptionChange = {},
            onScheduleChange = {},
            onPriceSelected = {},
            onMarkerMoved = { _, _ -> },
            onResubmit = {}
        )
    }
}
