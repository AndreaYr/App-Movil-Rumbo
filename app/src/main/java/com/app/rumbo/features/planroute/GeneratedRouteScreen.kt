package com.app.rumbo.features.planroute

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import com.app.rumbo.domain.model.StartPoint
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.component.MapBox
import com.app.rumbo.core.component.MapMarker
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.Teal
import com.app.rumbo.core.util.formatKm
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.model.SavedRouteStop
import com.app.rumbo.domain.model.Transport
import com.app.rumbo.domain.model.toSavedRoute
import com.mapbox.geojson.Point
import kotlinx.coroutines.launch

/** Comparte el ViewModel de "Planifica tu ruta": muestra el plan calculado con esos parámetros. */
@Composable
fun GeneratedRouteRoute(
    viewModel: PlanRouteViewModel,
    onBack: () -> Unit,
    onStopClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val route = remember(state.plan, state.preferences) { state.plan.toSavedRoute(state.preferences) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    RouteScreen(
        route = route,
        isSaved = state.isRouteSaved,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSave = {
            viewModel.onSaveRoute { saved ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (saved) "Ruta guardada en Mis rutas" else "No se pudo guardar la ruta"
                    )
                }
            }
        },
        onStopClick = onStopClick,
        start = state.preferences.start,
        modifier = modifier
    )
}

/** Mapa, datos y paradas de una ruta; sirve para la ruta recién generada y para las guardadas. */
@Composable
fun RouteScreen(
    route: SavedRoute,
    isSaved: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onStopClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    start: StartPoint? = null
) {
    val context = LocalContext.current
    var showShare by remember { mutableStateOf(false) }
    val markers = remember(route.stops) {
        route.stops.mapIndexed { index, stop ->
            MapMarker(
                id = stop.publicationId,
                latitude = stop.latitude,
                longitude = stop.longitude,
                // La última parada se resalta, como en el prototipo
                color = if (index == route.stops.lastIndex) Coral else Teal,
                label = (index + 1).toString()
            )
        }
    }
    val routeLine = remember(route.stops) {
        route.stops.map { Point.fromLngLat(it.longitude, it.latitude) }
    }

    if (showShare) {
        ShareRouteSheet(
            onTarget = { target ->
                showShare = false
                context.shareRoute(route, start, target)
            },
            onDismiss = { showShare = false }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomActions(
                isSaved = isSaved,
                canNavigate = route.stops.isNotEmpty(),
                onSave = onSave,
                onNavigate = { context.openRouteNavigation(route, start) }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            Box(Modifier.fillMaxWidth().height(260.dp)) {
                MapBox(
                    modifier = Modifier.fillMaxSize(),
                    center = routeLine.firstOrNull() ?: Point.fromLngLat(-75.6811, 4.5339),
                    zoom = 11.0,
                    markers = markers,
                    routeLine = routeLine,
                    clusterMarkers = false,
                    fitToMarkers = true,
                    showMyLocationButton = false,
                    onMarkerClick = { onStopClick(it.id) }
                )
                TopActions(
                    isSaved = isSaved,
                    canShare = route.stops.isNotEmpty(),
                    onBack = onBack,
                    onSave = onSave,
                    onShare = { showShare = true }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text(route.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${route.categories.joinToString { it.label }} · " +
                            "${route.transport.label} · ${route.budget.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Kpi(R.drawable.ic_map, route.distanceKm.formatKm(), "Distancia", Modifier.weight(1f))
                    Kpi(R.drawable.ic_schedule, route.totalMinutes.formatDuration(), "Tiempo total", Modifier.weight(1f))
                    Kpi(R.drawable.ic_place, route.stops.size.toString(), "Paradas", Modifier.weight(1f))
                }
                Text("Orden del recorrido", style = MaterialTheme.typography.titleMedium)
                route.stops.forEachIndexed { index, stop ->
                    StopRow(
                        number = index + 1,
                        stop = stop,
                        isLast = index == route.stops.lastIndex,
                        onClick = { onStopClick(stop.publicationId) }
                    )
                }
                if (route.stops.isEmpty()) {
                    Text(
                        "Sin paradas con estos parámetros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TopActions(isSaved: Boolean, canShare: Boolean, onBack: () -> Unit, onSave: () -> Unit, onShare: () -> Unit) {
    val colors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, colors = colors) {
            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Atrás")
        }
        Box(Modifier.weight(1f))
        Button(
            onClick = onSave,
            enabled = !isSaved,
            modifier = Modifier.height(40.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        ) {
            Icon(painterResource(R.drawable.ic_bookmark), null, Modifier.size(17.dp))
            Text(
                if (isSaved) "Guardada" else "Guardar",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        IconButton(
            onClick = onShare,
            enabled = canShare,
            modifier = Modifier.padding(start = 8.dp),
            colors = colors
        ) {
            Icon(painterResource(R.drawable.ic_share), contentDescription = "Compartir ruta")
        }
    }
}

@Composable
private fun BottomActions(isSaved: Boolean, canNavigate: Boolean, onSave: () -> Unit, onNavigate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onSave,
            enabled = !isSaved,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = Teal,
                contentColor = Color.White,
                disabledContainerColor = Teal.copy(alpha = 0.55f),
                disabledContentColor = Color.White
            )
        ) {
            Icon(painterResource(R.drawable.ic_bookmark), null, Modifier.size(19.dp))
            Text(
                if (isSaved) "Ruta guardada" else "Guardar ruta",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        OutlinedButton(
            onClick = onNavigate,
            enabled = canNavigate,
            modifier = Modifier.width(56.dp).height(52.dp),
            shape = MaterialTheme.shapes.large,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(painterResource(R.drawable.ic_navigation), contentDescription = "Iniciar recorrido", tint = Teal)
        }
    }
}

@Composable
private fun Kpi(@DrawableRes icon: Int, value: String, label: String, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(painterResource(icon), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun StopRow(number: Int, stop: SavedRouteStop, isLast: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(30.dp).background(if (isLast) Coral else Teal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
        Column(Modifier.weight(1f)) {
            Text(stop.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${stop.category.label} · ${stop.price.label} · ${stop.town}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(painterResource(R.drawable.ic_chevron_right), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun Int.formatDuration(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours == 0 -> "$minutes min"
        minutes == 0 -> "$hours h"
        else -> "$hours h $minutes"
    }
}

/** Abre las indicaciones de la ruta completa en la app de mapas o en el navegador. */
private fun Context.openRouteNavigation(route: SavedRoute, start: StartPoint?) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, directionsUrl(route, start).toUri()))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "No hay una app para abrir el recorrido", Toast.LENGTH_SHORT).show()
    }
}

/** Hoja inferior con las formas de compartir la ruta, cada una con su icono. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareRouteSheet(onTarget: (ShareTarget) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Compartir ruta", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            ShareTarget.entries.forEach { target ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(role = Role.Button) { onTarget(target) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painterResource(target.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .background(target.color.copy(alpha = 0.12f), CircleShape)
                            .padding(10.dp),
                        tint = target.color
                    )
                    Text(target.label, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

/** Color de cada red, para reconocerla de un vistazo. */
private val ShareTarget.color: Color
    get() = when (this) {
        ShareTarget.LINK -> Color(0xFF0E6E62)
        ShareTarget.WHATSAPP -> Color(0xFF25D366)
        ShareTarget.FACEBOOK -> Color(0xFF1877F2)
        ShareTarget.INSTAGRAM -> Color(0xFFE1306C)
        ShareTarget.EMAIL -> Color(0xFFD93025)
    }
