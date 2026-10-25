# Navegación en Jetpack Compose (Parte 2)

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Navegación en Jetpack Compose (Parte 2)  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

En la guía anterior configuramos la navegación básica para movernos entre las pantallas de inicio, login y registro. En esta segunda parte continuaremos expandiendo el flujo de la aplicación abordando el **paso de parámetros** entre pantallas, la creación de un **panel principal con barra de navegación inferior (Bottom Navigation)** y la navegación estructurada para ver los detalles de un reporte.

---

## Paso de parámetros entre pantallas

En aplicaciones reales es común necesitar pasar información de una pantalla a otra. Por ejemplo, al seleccionar un elemento de una lista (como un reporte), se debe navegar a la pantalla de detalle enviando el identificador (`id`) de ese elemento.

Con la integración de `kotlinx.serialization` en Navigation Compose, pasar parámetros es tan sencillo como agregar propiedades a las clases de rutas.

### 1. Definir la ruta con parámetros

Para pasar el ID de un reporte a la pantalla de detalle, agregamos esa propiedad a la clase correspondiente en `MainRoutes.kt`:

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

    // Ruta que recibe el ID del reporte a visualizar
    @Serializable
    data class ReportDetail(val reportId: String) : MainRoutes()

}
```

### 2. Recibir los datos en la pantalla destino

Para obtener los parámetros pasados a una ruta en Navigation Compose, se utiliza la función `toRoute()` sobre el `NavBackStackEntry`.

En `AppNavigation.kt`:

```kotlin
import androidx.navigation.toRoute

// ... dentro del NavHost

composable<MainRoutes.ReportDetail> { backStackEntry ->
    // Deserializa la ruta para extraer los argumentos pasados
    val route: MainRoutes.ReportDetail = backStackEntry.toRoute()
    val reportId = route.reportId

    ReportDetailScreen(
        reportId = reportId,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### 3. Navegar pasando el parámetro

Al invocar la navegación desde la pantalla de origen (por ejemplo, al hacer clic en un reporte de la lista):

```kotlin
navController.navigate(MainRoutes.ReportDetail(reportId = report.id))
```

---

## Navegación con Bottom Navigation Bar

Una vez que el usuario inicia sesión o decide explorar la aplicación, es común ofrecer una navegación principal basada en una barra inferior (*Bottom Navigation Bar*). 

Para implementar esta estructura, se suele abstraer un contenedor con `Scaffold` que aloje las distintas secciones principales (como la Lista de Reportes, el Mapa o el Perfil del usuario).

### 1. Definir las rutas del Bottom Navigation

Cree un archivo `BottomRoutes.kt` en el paquete `navigation`:

```kotlin
package com.example.demoapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed class BottomRoutes(val title: String, val icon: ImageVector) {

    @Serializable
    data object ReportsList : BottomRoutes("Reportes", Icons.Default.Home)

    @Serializable
    data object Map : BottomRoutes("Mapa", Icons.Default.LocationOn)

    @Serializable
    data object Profile : BottomRoutes("Perfil", Icons.Default.Person)

}
```

### 2. Crear la pantalla del contenedor principal (`MainScreen`)

Cree un composable `MainScreen.kt` en el paquete `features/main` (o dentro de `navigation`) que gestione el `Scaffold` con el `NavigationBar`:

```kotlin
package com.example.demoapp.features.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.demoapp.features.map.MapScreen
import com.example.demoapp.features.profile.ProfileScreen
import com.example.demoapp.features.report.list.ReportListScreen
import com.example.demoapp.navigation.BottomRoutes

@Composable
fun MainScreen(
    onReportClick: (String) -> Unit
) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomRoutes.ReportsList,
        BottomRoutes.Map,
        BottomRoutes.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val isSelected = currentDestination?.route?.contains(screen::class.qualifiedName.orEmpty()) == true

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(screen) {
                                // Pop up back to the start destination to avoid building up a large stack
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies when reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomRoutes.ReportsList,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<BottomRoutes.ReportsList> {
                ReportListScreen(
                    onReportClick = { reportId -> onReportClick(reportId) }
                )
            }
            composable<BottomRoutes.Map> {
                MapScreen()
            }
            composable<BottomRoutes.Profile> {
                ProfileScreen()
            }
        }
    }
}
```

### 3. Integrar `MainScreen` en las rutas principales

Agregue la ruta `Main` en `MainRoutes.kt`:

```kotlin
@Serializable
data object Main : MainRoutes()
```

Y actualice el `NavHost` principal en `AppNavigation.kt`:

```kotlin
composable<MainRoutes.Main> {
    MainScreen(
        onReportClick = { reportId ->
            navController.navigate(MainRoutes.ReportDetail(reportId))
        }
    )
}
```

---

## Limpieza del Back Stack (`popUpTo`)

Al realizar transiciones como iniciar sesión o registrarse, no se desea que el usuario vuelva a la pantalla de inicio o login al presionar el botón "atrás".

Para remover pantallas previas del back stack al navegar, se usa el bloque de configuración con `popUpTo`:

```kotlin
navController.navigate(MainRoutes.Main) {
    popUpTo(MainRoutes.Home) {
        inclusive = true // Elimina también la pantalla Home del back stack
    }
}
```

De este modo, cuando el usuario presiona "atrás" desde la pantalla principal (`Main`), la aplicación se cierra en lugar de retornar al flujo de bienvenida o login.

---

## Resumen

En esta guía aprendimos a:
1. Pasar parámetros fuertemente tipados utilizando las capacidades de `kotlinx.serialization` en Navigation Compose.
2. Recuperar parámetros en composables destino usando `toRoute()`.
3. Organizar una estructura con `NavigationBar` y `NavHost` anidado para las secciones principales.
4. Gestionar el *back stack* con `popUpTo` para un comportamiento de navegación limpio y profesional.