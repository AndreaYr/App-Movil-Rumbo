# Manejo de Permisos en Jetpack Compose

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Manejo de Permisos en Jetpack Compose  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

Las aplicaciones móviles a menudo necesitan acceder a funciones sensibles del dispositivo, como la ubicación GPS, la cámara, los contactos o el almacenamiento. Para proteger la privacidad del usuario, Android requiere solicitar **permisos en tiempo de ejecución (Runtime Permissions)** antes de acceder a estas características.

En esta guía abordaremos cómo declarar permisos en el archivo `AndroidManifest.xml` y cómo gestionarlos de forma reactiva y moderna dentro de Jetpack Compose utilizando la biblioteca **Accompanist Permissions**.

---

## Tipos de Permisos en Android

1. **Permisos normales (Normal Permissions):** Permisos que no representan un riesgo directo para la privacidad (ej. acceso a Internet o red WiFi). Se declaran en el `AndroidManifest.xml` y se conceden automáticamente al instalar la aplicación.
2. **Permisos peligrosos (Dangerous / Runtime Permissions):** Permisos que acceden a datos privados o hardware del dispositivo (ej. cámara, ubicación exacta, micrófono). Requieren ser declarados en el manifest **y solicitados explícitamente al usuario en tiempo de ejecución**.

---

## Biblioteca Accompanist Permissions

**Accompanist** es un grupo de bibliotecas complementarias para Jetpack Compose creadas por Google. La biblioteca `accompanist-permissions` simplifica el flujo de solicitud, verificación y manejo de estados de permisos dentro de composables.

---

# Integración en el proyecto

## 1. Agregar dependencias

Agregue la versión de Accompanist en `libs.versions.toml`:

```toml
[versions]
accompanist = "0.37.2"

[libraries]
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación (`app`), agregue la siguiente dependencia:

```kotlin
dependencies {
    implementation(libs.accompanist.permissions)
}
```

Sincronice el proyecto.

---

## 2. Declarar permisos en `AndroidManifest.xml`

Antes de solicitar un permiso en código Compose, este **debe** estar declarado en el archivo `AndroidManifest.xml`. 

Por ejemplo, para acceder a la ubicación del usuario (necesaria para colocar el reporte en el mapa) y a la cámara:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permisos de ubicación -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Permiso de cámara -->
    <uses-permission android:name="android.permission.CAMERA" />

    <application
        ... >
        ...
    </application>
</manifest>
```

---

## 3. Manejo de un Permiso Simple en Compose

Para solicitar un solo permiso (por ejemplo, el acceso a la cámara al adjuntar una imagen a un reporte), se utiliza el estado `rememberPermissionState`.

### Ejemplo: Formulario con acceso a la Cámara

```kotlin
package com.example.demoapp.features.report.create

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionSampleScreen() {
    // Estado del permiso de cámara
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    Column(modifier = Modifier.padding(16.dp)) {
        when {
            // Caso 1: El permiso ha sido concedido
            cameraPermissionState.status.isGranted -> {
                Text("El permiso de la cámara ha sido concedido. ¡Puedes tomar fotos!")
                // Composable o lógica para abrir la cámara
            }

            // Caso 2: Se debe mostrar una justificación (el usuario rechazó una vez)
            cameraPermissionState.status.shouldShowRationale -> {
                Text("La cámara es necesaria para adjuntar una fotografía al reporte de problema urbano.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Solicitar permiso de nuevo")
                }
            }

            // Caso 3: El permiso no ha sido solicitado aún o fue denegado de forma permanente
            else -> {
                Text("Se requiere acceso a la cámara para tomar fotos de los reportes.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Conceder permiso de cámara")
                }
            }
        }
    }
}
```

---

## 4. Manejo de Múltiples Permisos en Compose

Cuando una pantalla requiere varios permisos al mismo tiempo (por ejemplo, **Ubicación Fina** y **Ubicación Coordinada** para mostrar el mapa), se utiliza `rememberMultiplePermissionsState`.

### Ejemplo: Pantalla de Mapa con Permisos de Ubicación

```kotlin
package com.example.demoapp.features.map

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreenWithPermissions() {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    if (locationPermissionsState.allPermissionsGranted) {
        // Mostrar el componente de mapa si todos los permisos están concedidos
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Mapa cargado con éxito. Ubicación del usuario disponible.")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (locationPermissionsState.shouldShowRationale) {
                "El acceso a la ubicación es indispensable para mostrar los reportes cercanos a tu posición."
            } else {
                "Esta función requiere acceso a la ubicación. Por favor concede el permiso para continuar."
            }

            Text(text = textToShow)
            
            Button(
                onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Solicitar permisos de ubicación")
            }
        }
    }
}
```

---

## Buenas Prácticas

1. **Solicitar en contexto:** No solicite todos los permisos al abrir la aplicación por primera vez. Pida el permiso únicamente cuando el usuario intente realizar una acción que lo requiera (ejemplo: al presionar "Tomar foto" o al entrar a la pantalla del mapa).
2. **Explicar el Porqué (*Rationale*):** Si el usuario deniega un permiso la primera vez, responda adecuadamente evaluando `shouldShowRationale` para mostrar un diálogo o texto explicativo sobre el beneficio de concederlo.
3. **Manejar la denegación permanente:** Si el usuario selecciona "No volver a preguntar" o rechaza el permiso varias veces, la llamada a `launchPermissionRequest()` ya no mostrará el diálogo del sistema. En ese caso, proporcione un botón para redirigir al usuario a la configuración de la app en el dispositivo (`Settings`).

---

## Resumen

En esta guía aprendimos a:

1. Declarar permisos del sistema en el archivo `AndroidManifest.xml`.
2. Integrar la biblioteca **Accompanist Permissions** en nuestro entorno Compose.
3. Gestionar estados de permisos individuales con `rememberPermissionState`.
4. Manejar grupos de permisos con `rememberMultiplePermissionsState`.
5. Proveer una experiencia de usuario clara ante diferentes respuestas (otorgado, justificación o denegado).