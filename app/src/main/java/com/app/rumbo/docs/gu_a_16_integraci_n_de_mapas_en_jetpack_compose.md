# Integración de Mapas en Jetpack Compose

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Integración de Mapas en Jetpack Compose  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

En aplicaciones móviles que gestionan información georreferenciada —como la app de reportes urbanos que hemos estado desarrollando— la visualización de un mapa interactivo es un componente fundamental. 

En esta guía integraremos **Google Maps** en Jetpack Compose utilizando la librería oficial **Maps Compose**. Aprenderemos a configurar la API Key de Google Maps, renderizar un mapa interactivo, personalizar la cámara, agregar marcadores dinámicos (*Markers*) para representar los reportes y capturar la selección de ubicaciones por parte del usuario.

---

## Requisitos y Configuración de Google Maps API

Para utilizar Google Maps en Android se requiere obtener una clave de API (*API Key*) desde la consola de Google Cloud:

1. Ingrese a la [Google Cloud Console](https://console.cloud.google.com/).
2. Cree un proyecto nuevo o seleccione uno existente.
3. En la sección **APIs y servicios**, habilite la API **Maps SDK for Android**.
4. Vaya a **Credenciales**, cree una **Clave de API (API Key)** y copie su valor.

---

# Integración en el proyecto

## 1. Agregar dependencias

En el archivo `libs.versions.toml`, agregue la versión y las librerías necesarias para Maps Compose:

```toml
[versions]
mapsCompose = "6.4.0"
playServicesMaps = "19.0.0"

[libraries]
maps-compose = { module = "com.google.maps.android:maps-compose", version.ref = "mapsCompose" }
play-services-maps = { module = "com.google.android.gms:play-services-maps", version.ref = "playServicesMaps" }
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación (`app`), agregue las dependencias:

```kotlin
dependencies {
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
}
```

Sincronice el proyecto.

---

## 2. Configurar `AndroidManifest.xml`

Declare el meta-data con su API Key dentro de la etiqueta `<application>` en `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permisos de Ubicación -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application ...>

        <!-- Reemplaza YOUR_API_KEY por tu clave de API real de Google Cloud -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_API_KEY" />

    </application>
</manifest>
```

---

## 3. Composable `GoogleMap` Básico

El componente principal que ofrece la librería es `GoogleMap`. Para controlar la posición inicial de la cámara se utiliza `rememberCameraPositionState`.

### Ejemplo de Mapa Básico centrado en Armenia, Quindío:

```kotlin
package com.example.demoapp.features.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun SimpleMapScreen() {
    // Coordenadas iniciales (ejemplo: Armenia, Colombia)
    val armenia = LatLng(4.533892, -75.679788)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(armenia, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
}
```

---

## 4. Agregar Marcadores (*Markers*) Dinámicos

Para mostrar los reportes en el mapa, recorremos la lista de entidades `Report` y agregamos un composable `Marker` o `MarkerState` por cada uno.

### Ejemplo: Visualizar Reportes en el Mapa

```kotlin
package com.example.demoapp.features.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.demoapp.domain.model.Report
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onReportClick: (String) -> Unit = {}
) {
    val reports by viewModel.reports.collectAsState()
    
    val armenia = LatLng(4.533892, -75.679788)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(armenia, 13f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Renderizar un marcador por cada reporte en el mapa
        reports.forEach { report ->
            val position = LatLng(report.location.latitude, report.location.longitude)
            
            Marker(
                state = MarkerState(position = position),
                title = report.title,
                snippet = report.description,
                onInfoWindowClick = {
                    onReportClick(report.id)
                }
            )
        }
    }
}
```

---

## 5. Selección de Ubicación (Creación de Reportes)

Cuando el usuario desea crear un nuevo reporte, es útil permitirle tocar una ubicación en el mapa para capturar las coordenadas de latitud y longitud.

Se utiliza el parámetro `onMapClick` del composable `GoogleMap`:

```kotlin
package com.example.demoapp.features.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationPickerMapScreen(
    onLocationSelected: (LatLng) -> Unit
) {
    val defaultLocation = LatLng(4.533892, -75.679788)
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedLocation = latLng
            }
        ) {
            selectedLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Ubicación del problema"
                )
            }
        }

        selectedLocation?.let { location ->
            Button(
                onClick = { onLocationSelected(location) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Confirmar Ubicación")
            }
        }
    }
}
```

---

## 6. Personalizar Propiedades del Mapa y UI

`GoogleMap` permite personalizar la apariencia y los controles visuales mediante los objetos `MapProperties` y `MapUiSettings`:

```kotlin
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings

@Composable
fun CustomizedMapScreen() {
    val properties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.HYBRID, // Muestra mapa híbrido (satélite + calles)
                isMyLocationEnabled = false // Requiere permisos de ubicación concedidos
            )
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true,
                compassEnabled = true
            )
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = properties,
        uiSettings = uiSettings
    )
}
```

---

## Resumen

En esta guía aprendimos a:

1. Obtener y configurar una clave de API de **Google Maps** en la consola de Google Cloud y el manifest.
2. Integrar la librería **Maps Compose** para renderizar mapas interactivos de forma declarativa.
3. Manejar la cámara con `rememberCameraPositionState`.
4. Mostrar marcadores dinámicos (*Markers*) asociados a los datos de la app.
5. Capturar clics del usuario en el mapa para seleccionar coordenadas geográficas al crear reportes.
6. Personalizar el tipo de mapa y los controles de UI mediante `MapProperties` y `MapUiSettings`.