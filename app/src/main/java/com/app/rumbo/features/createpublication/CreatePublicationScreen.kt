package com.app.rumbo.features.createpublication

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.rumbo.R
import com.app.rumbo.core.component.color
import com.app.rumbo.core.component.iconRes
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.Gold
import com.app.rumbo.core.theme.GoldContainer
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// Rango aproximado cubierto por el mapa del Quindío (maqueta hasta integrar Google Maps/Mapbox)
private const val MIN_LATITUDE = 4.40
private const val MAX_LATITUDE = 4.78
private const val MIN_LONGITUDE = -75.90
private const val MAX_LONGITUDE = -75.35

private const val NOTICE_TEXT_COLOR = 0xFF8A6A0B
private const val MAP_LAND = 0xFFE4EEE9
private const val MAP_GREEN = 0xFFCFE3D6

@Composable
fun CreatePublicationRoute(
    onClose: () -> Unit,
    onPublished: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatePublicationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CreatePublicationScreen(
        state = state,
        events = viewModel.events,
        onClose = onClose,
        onPublished = onPublished,
        onPhotosAdded = viewModel::onPhotosAdded,
        onRemovePhoto = viewModel::onRemovePhoto,
        onTitleChange = viewModel::onTitleChange,
        onCategorySelected = viewModel::onCategorySelected,
        onDescriptionChange = viewModel::onDescriptionChange,
        onScheduleChange = viewModel::onScheduleChange,
        onPriceSelected = viewModel::onPriceSelected,
        onMarkerMoved = viewModel::onMarkerMoved,
        onUseMyLocation = viewModel::onUseMyLocation,
        onSaveDraft = viewModel::onSaveDraft,
        onPublish = viewModel::onPublish,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicationScreen(
    state: CreatePublicationUiState,
    events: Flow<CreatePublicationEvent>,
    onClose: () -> Unit,
    onPublished: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onScheduleChange: (String) -> Unit,
    onPriceSelected: (PriceLevel) -> Unit,
    onMarkerMoved: (Double, Double) -> Unit,
    onUseMyLocation: () -> Unit,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS)
    ) { uris -> onPhotosAdded(uris.map { it.toString() }) }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                CreatePublicationEvent.DraftSaved -> snackbarHostState.showSnackbar("Borrador guardado")
                is CreatePublicationEvent.Published -> onPublished(state.title)
            }
        }
    }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            CreateTopBar(onClose)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ModerationNotice()

                FormField("Fotografías · mínimo 1, máximo $MAX_PHOTOS") {
                    PhotosField(
                        photos = state.photos,
                        canAdd = state.canAddPhoto,
                        error = state.photosError,
                        onAdd = {
                            pickPhotos.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemove = onRemovePhoto
                    )
                }

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
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LocationMap(
                            latitude = state.latitude,
                            longitude = state.longitude,
                            markerColor = Coral,
                            onMarkerMoved = onMarkerMoved,
                            modifier = Modifier.fillMaxWidth().height(190.dp)
                        )
                        OutlinedButton(
                            onClick = onUseMyLocation,
                            modifier = Modifier.align(Alignment.End).height(44.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text("Usar mi ubicación", style = MaterialTheme.typography.titleSmall)
                            Icon(
                                painterResource(R.drawable.ic_compass),
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp).size(18.dp)
                            )
                        }
                    }
                }
            }

            ActionBar(
                isSaving = state.isSaving,
                onSaveDraft = onSaveDraft,
                onPublish = onPublish
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTopBar(onClose: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Nuevo punto de interés", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = "Cerrar")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun ModerationNotice() {
    val message = buildAnnotatedString {
        append("Tu publicación será revisada por un moderador antes de aparecer públicamente. Quedará como ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Pendiente de verificación") }
        append(".")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(GoldContainer)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(painterResource(R.drawable.ic_shield), contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = Color(NOTICE_TEXT_COLOR))
    }
}

@Composable
private fun PhotosField(
    photos: List<Photo>,
    canAdd: Boolean,
    error: String?,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
            if (canAdd) {
                item { AddPhotoBox(onClick = onAdd) }
            }
            itemsIndexed(photos) { index, photo ->
                PhotoThumb(
                    photo = photo,
                    isMain = index == 0,
                    onRemove = { onRemove(index) }
                )
            }
        }
        error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun AddPhotoBox(onClick: () -> Unit) {
    val strokeColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .size(width = 96.dp, height = 96.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .drawDashedBorder(strokeColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(painterResource(R.drawable.ic_camera), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text("Agregar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun Modifier.drawDashedBorder(color: Color) = this.then(
    Modifier.drawWithContent {
        drawContent()
        val strokeWidth = 2.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = CornerRadius(14.dp.toPx()),
            style = Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
        )
    }
)

@Composable
private fun PhotoThumb(photo: Photo, isMain: Boolean, onRemove: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Box(Modifier.size(96.dp).clip(shape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        AsyncImage(
            model = photo.url,
            contentDescription = photo.description,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            if (isMain) "foto 1 principal" else "foto",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(Color(0x99000000), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = "Quitar foto",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CategoryPicker(selected: Category, onSelected: (Category) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
        items(Category.entries) { category ->
            CategoryChip(category = category, selected = category == selected, onClick = { onSelected(category) })
        }
    }
}

@Composable
private fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    val accent = category.color
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) accent else MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(painterResource(category.iconRes), contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Text(
            category.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DescriptionField(value: String, onValueChange: (String) -> Unit, error: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Describe el sitio a visitar.") },
            minLines = 4,
            isError = error != null,
            shape = MaterialTheme.shapes.medium,
            colors = rumboTextFieldColors(),
            supportingText = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text(" ")
                    }
                    Text(
                        "${value.length} / $MAX_DESCRIPTION_LENGTH",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PriceField(price: PriceLevel, onSelected: (PriceLevel) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        SelectorRow(iconRes = R.drawable.ic_money, text = price.label, onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PriceLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.label) },
                    onClick = {
                        onSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectorRow(iconRes: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RumboTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    supportingText: String? = null,
    leadingIcon: Int? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(placeholder) },
            isError = isError,
            shape = MaterialTheme.shapes.medium,
            leadingIcon = if (leadingIcon != null) {
                { Icon(painterResource(leadingIcon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                null
            },
            supportingText = if (supportingText != null) {
                { Text(supportingText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            } else {
                null
            },
            colors = rumboTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun rumboTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
private fun LocationMap(
    latitude: Double,
    longitude: Double,
    markerColor: Color,
    onMarkerMoved: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val markerSize = 34.dp
    val markerPx = with(density) { markerSize.toPx() }

    val markerOffset = remember(latitude, longitude, boxSize, markerPx) {
        val width = boxSize.width.toFloat()
        val height = boxSize.height.toFloat()
        if (width <= 0f || height <= 0f) {
            Offset.Unspecified
        } else {
            val fractionX = ((longitude - MIN_LONGITUDE) / (MAX_LONGITUDE - MIN_LONGITUDE)).toFloat().coerceIn(0f, 1f)
            val fractionY = ((MAX_LATITUDE - latitude) / (MAX_LATITUDE - MIN_LATITUDE)).toFloat().coerceIn(0f, 1f)
            Offset(fractionX * width - markerPx / 2f, fractionY * height - markerPx)
        }
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color(MAP_LAND))
            .onSizeChanged { boxSize = it }
            .pointerInput(boxSize) {
                if (boxSize.width == 0 || boxSize.height == 0) return@pointerInput
                fun report(position: Offset) {
                    val fractionX = (position.x / boxSize.width).coerceIn(0f, 1f)
                    val fractionY = (position.y / boxSize.height).coerceIn(0f, 1f)
                    onMarkerMoved(
                        MAX_LATITUDE - fractionY * (MAX_LATITUDE - MIN_LATITUDE),
                        MIN_LONGITUDE + fractionX * (MAX_LONGITUDE - MIN_LONGITUDE)
                    )
                }
                detectDragGestures(
                    onDragStart = { report(it) },
                    onDrag = { change, _ ->
                        change.consume()
                        report(change.position)
                    }
                )
            }
    ) {
        Canvas(Modifier.matchParentSize()) {
            val green = Color(MAP_GREEN)
            val road = Color.White
            drawCircle(green, radius = size.minDimension * 0.30f, center = Offset(size.width * 0.82f, size.height * 0.78f))
            drawCircle(green, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.12f, size.height * 0.24f))
            drawLine(road, Offset(0f, size.height * 0.32f), Offset(size.width, size.height * 0.24f), strokeWidth = 7f)
            drawLine(road, Offset(size.width * 0.42f, 0f), Offset(size.width * 0.50f, size.height), strokeWidth = 7f)
            drawLine(road, Offset(0f, size.height * 0.85f), Offset(size.width, size.height * 0.70f), strokeWidth = 7f)
        }

        if (markerOffset != Offset.Unspecified) {
            Icon(
                painterResource(R.drawable.ic_place),
                contentDescription = "Marcador de la ubicación",
                tint = markerColor,
                modifier = Modifier
                    .offset { IntOffset(markerOffset.x.roundToInt(), markerOffset.y.roundToInt()) }
                    .size(markerSize)
            )
        }

        Text(
            String.format(Locale.US, "Lat %.4f · Lng %.4f", latitude, longitude),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(Color(0xCCFFFFFF), MaterialTheme.shapes.small)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ActionBar(isSaving: Boolean, onSaveDraft: () -> Unit, onPublish: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSaveDraft,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Guardar", style = MaterialTheme.typography.titleSmall)
            }
            Button(
                onClick = onPublish,
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
                        "Publicar para revisión",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun CreatePublicationScreenPreview() {
    RumboTheme {
        CreatePublicationScreen(
            state = CreatePublicationUiState(),
            events = emptyFlow(),
            onClose = {},
            onPublished = { _ -> },
            onPhotosAdded = {},
            onRemovePhoto = {},
            onTitleChange = {},
            onCategorySelected = {},
            onDescriptionChange = {},
            onScheduleChange = {},
            onPriceSelected = {},
            onMarkerMoved = { _, _ -> },
            onUseMyLocation = {},
            onSaveDraft = {},
            onPublish = {}
        )
    }
}
