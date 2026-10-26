# Navegación en Jetpack Compose (Parte 1)

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Navegación en Jetpack Compose  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

Hasta ahora, para ver cada pantalla había que cambiar el composable que se llama desde `MainActivity`. En esta guía las pantallas se conectan con Navigation Compose, y se abordan la navegación básica, el paso de parámetros entre pantallas y el manejo del botón “atrás”.

---

## Navigation Compose

Navigation Compose es la biblioteca oficial de navegación para Jetpack Compose. Permite definir rutas, gestionar el back stack y pasar datos entre pantallas.

### ¿Qué es un back stack?

El back stack es una estructura de datos que mantiene un registro de las pantallas visitadas por el usuario en una aplicación. Cada vez que el usuario navega a una nueva pantalla, esta se agrega al tope del back stack. Cuando el usuario presiona el botón de retroceso, la pantalla actual se elimina del tope del back stack y se muestra la pantalla anterior.

Esta imagen muestra un ejemplo de cómo funciona el back stack en una aplicación móvil:

![Back Stack](/guias-apps-moviles/res/back_stack.svg)

Navigation Compose gestiona el back stack automáticamente. Aun así, hay que entenderlo para decidir, por ejemplo, qué pantallas deben salir de la pila después de iniciar sesión.

### NavHost

`NavHost` es el contenedor de las pantallas de la aplicación y el lugar donde se configura la navegación. En el siguiente diagrama, el `NavHost` vive dentro de `MainActivity`, asocia cada ruta con su pantalla mediante `composable` y muestra la pantalla de la ruta actual. Al iniciar la aplicación, esa ruta es la definida en `startDestination`. Para cambiar de pantalla se usa el `navController`, por ejemplo con `navigate(MainRoutes.Login)`.

![NavHost](/guias-apps-moviles/res/navhost.svg)

### Nested NavHost

Cuando una sección de la aplicación tiene su propia navegación interna (por ejemplo, un panel con barra inferior), se usa un `NavHost` anidado dentro de otro. Cada `NavHost` maneja su propio flujo de pantallas.

Así se organizan los `NavHost` en el proyecto que construiremos:

![Nested NavHost](/guias-apps-moviles/res/nested_navhost.svg)

### Configuración inicial

Navigation Compose usa `kotlinx-serialization` para las rutas. Agregue ambas bibliotecas y el plugin en `libs.versions.toml`:

```toml
[versions]
kotlin = "2.4.20" # Debe usarse kotlin 2.x.x
navigationCompose = "2.10.2"
kotlinxSerialization = "1.11.0"

[libraries]
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación, modifique lo siguiente:

```kotlin
plugins {
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
```

Finalmente, sincronice el proyecto para descargar las nuevas dependencias.

Para más información sobre la navegación en Jetpack Compose, puede consultar la documentación oficial: [Navigation in Jetpack Compose](https://developer.android.com/develop/ui/compose/navigation).

---

# Implementación de la navegación básica

Primero se conectará la pantalla de inicio con las de Login y Registro creadas en guías anteriores.

## 1. Configurar NavHost

El `NavHost` y sus rutas irán en un composable propio. Cree un nuevo paquete llamado `navigation` en la raíz del proyecto (al mismo nivel de `core`, `features` y `MainActivity.kt`) y dentro de este paquete, cree un archivo llamado `AppNavigation.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.demoapp.features.home.HomeScreen
import com.example.demoapp.features.login.LoginScreen
import com.example.demoapp.features.register.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // El Surface toma el color de fondo del tema, así las pantallas
    // se ven bien en modo claro y oscuro
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = MainRoutes.Home // Primera pantalla al abrir la aplicación
        ) {

            composable<MainRoutes.Home> {
                HomeScreen()
            }

            composable<MainRoutes.Login> {
                LoginScreen()
            }

            composable<MainRoutes.Register> {
                RegisterScreen()
            }

        }
    }
}
```

Todas las pantallas que hemos creado anteriormente (`HomeScreen`, `LoginScreen` y `RegisterScreen`) se integran en el `NavHost` mediante la función `composable`, que asocia cada ruta con su respectivo composable.

## 2. Definir las rutas de navegación

Cree un archivo `MainRoutes.kt` en el mismo paquete `navigation` para definir las rutas de navegación de nuestra aplicación. Utilizaremos una `sealed class` para representar las diferentes rutas.

```kotlin
package com.example.demoapp.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {

    @Serializable
    data object Home : MainRoutes()

    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object Register : MainRoutes()

}
```

Como es una `sealed class`, las rutas posibles quedan fijas y el compilador las conoce todas. La anotación `@Serializable` permite que Navigation Compose convierta cada ruta, con sus parámetros, en texto.

## 3. Navegar entre pantallas

Ahora los botones de la pantalla de inicio deben navegar a Login y Registro. Modifique el composable `HomeScreen` para que reciba una función por cada botón y la invoque en su `onClick`; el resto del composable no cambia:

```kotlin
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit, // Función para navegar a la pantalla de Login
    onNavigateToRegister: () -> Unit // Función para navegar a la pantalla de Registro
){
    Column(/* igual que antes */) {

        // Image y Text igual que antes...

        Row(/* igual que antes */) {

            Button(
                onClick = onNavigateToLogin
            ) {
                Text(text = "Iniciar sesión")
            }

            Button(
                onClick = onNavigateToRegister
            ) {
                Text(text = "Crear una cuenta")
            }
        }
    }
}
```

## 4. Ajustes en `AppNavigation.kt`

Ahora, regresemos al archivo `AppNavigation.kt` y ajustemos la llamada a `HomeScreen` para pasar las funciones de navegación adecuadas.

```kotlin
composable<MainRoutes.Home> {
    HomeScreen(
        onNavigateToLogin = {
            navController.navigate(MainRoutes.Login)
        },
        onNavigateToRegister = {
            navController.navigate(MainRoutes.Register)
        }
    )
}
```

`HomeScreen` no sabe a dónde lleva cada botón y se limita a avisar que se presionó. Quien decide el destino es `AppNavigation`, y así todo el flujo entre pantallas se lee en un solo archivo.

## 5. Actualizar el punto de entrada de la aplicación

Finalmente, asegúrese de que el punto de entrada de su aplicación utilice el nuevo sistema de navegación. En su archivo `MainActivity.kt`, reemplace la pantalla que tenga dentro de `DemoAppTheme` (al terminar la guía de entidades era `ReportListScreen(...)`) por `AppNavigation()`. Puede quitar los imports de pantallas que queden en gris.

```kotlin
setContent {
    DemoAppTheme {
        // La navegación de la aplicación se maneja aquí
        AppNavigation()
    }
}
```

Es necesario agregar el siguiente import al inicio de `MainActivity.kt`:

```kotlin
import com.example.demoapp.navigation.AppNavigation
```