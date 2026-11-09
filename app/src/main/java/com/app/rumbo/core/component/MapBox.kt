package com.app.rumbo.core.component

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.app.rumbo.R
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.Teal
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.DefaultViewportTransitionOptions
import kotlinx.coroutines.awaitCancellation

// Centro de Calarcá, Quindío. Mapbox usa el orden (longitud, latitud)
private val CALARCA = Point.fromLngLat(-75.6811, 4.5339)
private const val MARKER_MAX_FIT_ZOOM = 15.0
private const val FOCUS_ZOOM = 16.0

// Estilo claro con relieve y zonas verdes, acorde con el prototipo
private const val MAP_STYLE = Style.OUTDOORS

/**
 * Marcador del mapa. El mapa no conoce el dominio: cada pantalla convierte sus datos a este modelo.
 * @param icon icono blanco dentro de un pin; se ignora si hay [label].
 * @param label texto corto (por ejemplo el número de una parada) dentro de un círculo.
 */
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val color: Color,
    @param:DrawableRes val icon: Int? = null,
    val label: String? = null
) {
    val point: Point get() = Point.fromLngLat(longitude, latitude)
}

/**
 * Mapa reutilizable: encapsula la cámara, los marcadores, los permisos y la ubicación del usuario.
 * No guarda estado de la pantalla ni decide qué pasa con los toques; solo avisa con sus callbacks.
 *
 * @param center y [zoom] solo se usan al componerse por primera vez.
 * @param selectedPoint marcador suelto que controla la pantalla; null = ninguno.
 * @param focusPoint si no es null, la cámara vuela hasta ese punto y no se encuadran los marcadores.
 * @param draggableSelectedPoint permite arrastrar [selectedPoint]; al soltarlo se llama a [onMapClick].
 * @param routeLine puntos unidos por una línea, en orden.
 * @param fitToMarkers encuadra la cámara para que se vean todos los marcadores.
 * @param interactive si es false, el mapa no responde a gestos y deja pasar el scroll de la pantalla.
 * @param locateUserRequest cada incremento equivale a tocar "Mi ubicación", para pantallas con su propio botón.
 * @param onUserLocation entrega la posición del usuario cada vez que se pide su ubicación.
 */
@Composable
fun MapBox(
    modifier: Modifier = Modifier,
    center: Point = CALARCA,
    zoom: Double = 8.5,
    markers: List<MapMarker> = emptyList(),
    selectedPoint: Point? = null,
    focusPoint: Point? = null,
    selectedPointColor: Color = Coral,
    draggableSelectedPoint: Boolean = false,
    routeLine: List<Point> = emptyList(),
    clusterMarkers: Boolean = true,
    fitToMarkers: Boolean = false,
    interactive: Boolean = true,
    showMyLocationButton: Boolean = true,
    myLocationButtonAlignment: Alignment = Alignment.TopEnd,
    locateUserRequest: Int = 0,
    onMarkerClick: (MapMarker) -> Unit = {},
    onMapClick: (Point) -> Unit = {},
    onUserLocation: (Point) -> Unit = {}
) {
    // Sin token el SDK no puede cargar el mapa; en las vistas previas no hay motor nativo
    val accessToken = stringResource(R.string.mapbox_access_token)
    if (LocalInspectionMode.current || accessToken.isBlank()) {
        MapUnavailable(modifier)
        return
    }

    val context = LocalContext.current
    val density = LocalDensity.current.density
    val currentOnUserLocation by rememberUpdatedState(onUserLocation)
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)

    // Cada incremento es una petición para centrar la cámara en el usuario
    var followUserRequest by remember { mutableIntStateOf(0) }
    val userLocation = remember { UserLocationTracker() }

    val permissionState = rememberLocationPermissionState { granted ->
        if (granted) {
            followUserRequest++
        } else {
            Toast.makeText(
                context,
                "Activa el permiso de ubicación para ver tu posición en el mapa",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    val locateUser: () -> Unit = {
        if (permissionState.hasPermission) {
            followUserRequest++
        } else {
            permissionState.requestPermission()
        }
    }
    LaunchedEffect(locateUserRequest) {
        if (locateUserRequest > 0) locateUser()
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(zoom)
            center(center)
        }
    }

    // Se guarda el id en cada marcador para saber cuál tocó el usuario
    val markersById = remember(markers) { markers.associateBy { it.id } }
    val annotations = remember(markers, density) {
        val icons = mutableMapOf<MarkerIconKey, Bitmap>()
        markers.map { marker ->
            val key = MarkerIconKey(marker.color, marker.icon, marker.label)
            PointAnnotationOptions()
                .withPoint(marker.point)
                .withData(JsonPrimitive(marker.id))
                .withIconImage(icons.getOrPut(key) { context.markerBitmap(key, density) })
                .withIconAnchor(if (marker.label != null) IconAnchor.CENTER else IconAnchor.BOTTOM)
        }
    }
    val selectedIcon = remember(selectedPointColor, density) {
        IconImage(context.markerBitmap(MarkerIconKey(selectedPointColor), density))
    }
    // Agrupa los marcadores que estén a menos de 60 píxeles entre sí
    val annotationConfig = remember(clusterMarkers) {
        if (clusterMarkers) {
            AnnotationConfig(
                annotationSourceOptions = AnnotationSourceOptions(
                    clusterOptions = ClusterOptions(clusterRadius = 60L)
                )
            )
        } else {
            AnnotationConfig()
        }
    }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = MAP_STYLE) },
            scaleBar = {},
            onMapClickListener = { point ->
                currentOnMapClick(point)
                true
            }
        ) {
            if (interactive) {
                // Dentro de una pantalla con scroll, el gesto iniciado sobre el mapa es del mapa
                MapEffect(Unit) { mapView -> mapView.keepGesturesFromParent() }

                // El punto azul aparece en cuanto hay permiso, sin mover la cámara
                MapEffect(permissionState.hasPermission) { mapView ->
                    val hasPermission = permissionState.hasPermission
                    mapView.location.updateSettings {
                        enabled = hasPermission
                        if (hasPermission) {
                            locationPuck = createDefault2DPuck(withBearing = true)
                            puckBearing = PuckBearing.COURSE
                            puckBearingEnabled = true
                        }
                    }
                    if (!hasPermission) return@MapEffect
                    val listener = OnIndicatorPositionChangedListener { point ->
                        userLocation.onPosition(point)?.let { currentOnUserLocation(it) }
                    }
                    mapView.location.addOnIndicatorPositionChangedListener(listener)
                    try {
                        awaitCancellation()
                    } finally {
                        mapView.location.removeOnIndicatorPositionChangedListener(listener)
                    }
                }

                // La clave cambia en cada petición, así el efecto se vuelve a ejecutar
                if (permissionState.hasPermission && followUserRequest > 0) {
                    MapEffect(followUserRequest) {
                        userLocation.request()?.let { currentOnUserLocation(it) }
                        mapViewportState.transitionToFollowPuckState(
                            defaultTransitionOptions = DefaultViewportTransitionOptions.Builder()
                                .maxDurationMs(1500)
                                .build()
                        )
                    }
                }
            }

            // Vuela al lugar elegido en el buscador
            MapEffect(focusPoint) {
                focusPoint?.let {
                    mapViewportState.flyTo(
                        CameraOptions.Builder().center(it).zoom(FOCUS_ZOOM).build(),
                        MapAnimationOptions.mapAnimationOptions { duration(1200L) }
                    )
                }
            }

            if (fitToMarkers && focusPoint == null && markers.isNotEmpty()) {
                MapEffect(markers) { mapView ->
                    val padding = 56.0 * density
                    mapView.mapboxMap.cameraForCoordinates(
                        markers.map { it.point },
                        CameraOptions.Builder().build(),
                        EdgeInsets(padding, padding, padding, padding),
                        MARKER_MAX_FIT_ZOOM,
                        null
                    ) { camera -> mapViewportState.setCameraOptions(camera) }
                }
            }

            if (routeLine.size > 1) {
                PolylineAnnotation(points = routeLine) {
                    lineColor = Teal
                    lineWidth = 3.0
                    lineOpacity = 0.7
                }
            }

            // Marcadores en una sola capa; rinde mejor y permite agruparlos
            if (annotations.isNotEmpty()) {
                PointAnnotationGroup(
                    annotations = annotations,
                    annotationConfig = annotationConfig
                ) {
                    iconAllowOverlap = true
                    interactionsState.onClicked { annotation ->
                        val marker = annotation.getData()?.asString?.let(markersById::get)
                        marker?.let(currentOnMarkerClick)
                        marker != null // true = el toque no llega al mapa
                    }
                }
            }

            selectedPoint?.let { point ->
                PointAnnotation(point = point) {
                    iconImage = selectedIcon
                    iconAnchor = IconAnchor.BOTTOM
                    interactionsState.isDraggable = draggableSelectedPoint
                    interactionsState.onDragFinished { currentOnMapClick(it.point) }
                }
            }
        }

        // Capa transparente encima del mapa: recibe los toques para que el mapa no los procese.
        // El arrastre no se consume, así que el scroll de la pantalla sigue funcionando
        if (!interactive) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {}
            )
        }

        if (showMyLocationButton && interactive) {
            SmallFloatingActionButton(
                onClick = locateUser,
                modifier = Modifier
                    .align(myLocationButtonAlignment)
                    .padding(12.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painterResource(R.drawable.ic_my_location),
                    contentDescription = "Mi ubicación",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MapUnavailable(modifier: Modifier) {
    Box(modifier.background(Color(0xFFE6EBE4)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(painterResource(R.drawable.ic_map), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Mapa no disponible: falta MAPBOX_ACCESS_TOKEN en local.properties",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Recuerda la última posición del usuario. El indicador solo avisa cuando la posición cambia,
 * así que una petición se responde con la última conocida o queda pendiente hasta que llegue una.
 */
private class UserLocationTracker {
    private var last: Point? = null
    private var pending = false

    fun request(): Point? = last.also { pending = it == null }

    fun onPosition(point: Point): Point? {
        last = point
        return if (pending) point.also { pending = false } else null
    }
}

@SuppressLint("ClickableViewAccessibility")
private fun android.view.View.keepGesturesFromParent() {
    setOnTouchListener { view, event ->
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            view.parent?.requestDisallowInterceptTouchEvent(true)
        }
        false
    }
}

private data class MarkerIconKey(
    val color: Color,
    @param:DrawableRes val icon: Int? = null,
    val label: String? = null
)

/** Dibuja un marcador como el del prototipo: círculo con número, o pin con icono y borde blanco. */
private fun Context.markerBitmap(key: MarkerIconKey, density: Float): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val border = 2.5f * density

    if (key.label != null) {
        val size = (32 * density).toInt()
        val center = size / 2f
        return createBitmap(size, size).also { bitmap ->
            val canvas = Canvas(bitmap)
            paint.color = android.graphics.Color.WHITE
            canvas.drawCircle(center, center, center, paint)
            paint.color = key.color.toArgb()
            canvas.drawCircle(center, center, center - border, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 13 * density
            paint.isFakeBoldText = true
            canvas.drawText(key.label, center, center - (paint.descent() + paint.ascent()) / 2, paint)
        }
    }

    val width = (38 * density).toInt()
    val height = (46 * density).toInt()
    val center = width / 2f
    fun pin(radius: Float, tipY: Float) = Path().apply {
        addCircle(center, center, radius, Path.Direction.CW)
        // Mismo sentido que el círculo: en sentido contrario, la zona común quedaría sin relleno
        moveTo(center + radius * 0.72f, center + radius * 0.69f)
        lineTo(center, tipY)
        lineTo(center - radius * 0.72f, center + radius * 0.69f)
        close()
    }
    return createBitmap(width, height).also { bitmap ->
        val canvas = Canvas(bitmap)
        paint.color = android.graphics.Color.WHITE
        canvas.drawPath(pin(center, height.toFloat()), paint)
        paint.color = key.color.toArgb()
        canvas.drawPath(pin(center - border, height - border * 1.6f), paint)

        val iconSize = (18 * density).toInt()
        val left = (center - iconSize / 2f).toInt()
        val top = (center - iconSize / 2f).toInt()
        val drawable = key.icon?.let { ContextCompat.getDrawable(this, it)?.mutate() }
        if (drawable != null) {
            drawable.setTint(android.graphics.Color.WHITE)
            drawable.setBounds(left, top, left + iconSize, top + iconSize)
            drawable.draw(canvas)
        } else {
            paint.color = android.graphics.Color.WHITE
            canvas.drawCircle(center, center, 5 * density, paint)
        }
    }
}

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

// Basta con la ubicación aproximada: desde Android 12 el usuario puede negar la precisa
private fun Context.hasLocationPermission(): Boolean =
    locationPermissions.any {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

/** Estado observable del permiso de ubicación. */
class LocationPermissionState(hasPermission: Boolean = false) {
    var hasPermission by mutableStateOf(hasPermission)
        internal set

    // Se asigna al crear el estado en el composable; lanza la solicitud de permiso
    var requestPermission: () -> Unit = {}
        internal set
}

/**
 * @param onPermissionResult se llama después del diálogo del sistema; `granted` es true
 * si se concedió la ubicación precisa o la aproximada.
 */
@Composable
fun rememberLocationPermissionState(
    onPermissionResult: (granted: Boolean) -> Unit = {}
): LocationPermissionState {
    val context = LocalContext.current
    val state = remember { LocationPermissionState(context.hasLocationPermission()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        state.hasPermission = granted
        onPermissionResult(granted)
    }
    state.requestPermission = { launcher.launch(locationPermissions) }

    // El permiso puede cambiar fuera de la app (desde Ajustes),
    // así que se vuelve a leer cada vez que la pantalla vuelve a primer plano
    LifecycleResumeEffect(state) {
        state.hasPermission = context.hasLocationPermission()
        onPauseOrDispose { }
    }
    return state
}
