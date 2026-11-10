package com.app.rumbo.features.planroute

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.rumbo.R
import com.app.rumbo.core.component.MapBox
import com.app.rumbo.core.theme.Teal
import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.features.createpublication.RumboTextField
import com.mapbox.geojson.Point

/**
 * Pantalla completa para elegir dónde empieza la ruta: mapa con un buscador de direcciones arriba.
 * El punto se puede fijar escribiendo una dirección, tocando el mapa, arrastrando el marcador o con la ubicación actual.
 */
@Composable
fun StartPointPicker(
    state: StartPickerUiState,
    onQueryChange: (String) -> Unit,
    onSuggestionPicked: (PlaceResult) -> Unit,
    onMapPoint: (Double, Double) -> Unit,
    onConfirm: () -> Unit,
    onUseMyLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isOpen) return
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            val selected = state.point?.let { Point.fromLngLat(it.longitude, it.latitude) }
            MapBox(
                modifier = Modifier.fillMaxSize(),
                center = selected ?: Point.fromLngLat(DEFAULT_LONGITUDE, DEFAULT_LATITUDE),
                zoom = 14.0,
                selectedPoint = selected,
                focusPoint = state.focus?.let { Point.fromLngLat(it.longitude, it.latitude) },
                draggableSelectedPoint = true,
                myLocationButtonAlignment = Alignment.CenterEnd,
                // Sin un punto previo, el mapa se abre en la ubicación del usuario y la fija como inicio
                locateUserRequest = if (state.point == null) 1 else 0,
                onMapClick = { onMapPoint(it.latitude(), it.longitude()) },
                onUserLocation = { onMapPoint(it.latitude(), it.longitude()) }
            )

            Column(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Cerrar")
                    }
                    Box(Modifier.weight(1f).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)) {
                        RumboTextField(
                            value = state.query,
                            onValueChange = onQueryChange,
                            placeholder = "Escribe la dirección de inicio",
                            leadingIcon = R.drawable.ic_search
                        )
                    }
                }
                Suggestions(state.suggestions, onSuggestionPicked)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    state.point?.label ?: "Toca el mapa o escribe una dirección",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onUseMyLocation,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(painterResource(R.drawable.ic_my_location), null, Modifier.size(18.dp), tint = Teal)
                        Text("Mi ubicación", modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = state.point != null,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) { Text("Usar este punto", maxLines = 1) }
                }
            }
        }
    }
}

@Composable
private fun Suggestions(places: List<PlaceResult>, onPick: (PlaceResult) -> Unit) {
    if (places.isEmpty()) return
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
    ) {
        places.forEach { place ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(place) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(place.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (place.address.isNotBlank()) {
                    Text(
                        place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Centro de Calarcá, Quindío
private const val DEFAULT_LATITUDE = 4.5339
private const val DEFAULT_LONGITUDE = -75.6811
