# Entidades del Dominio

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Entidades del Dominio  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

Una entidad del dominio representa un concepto central del negocio. En una aplicación de tareas, por ejemplo, sería la “Tarea”, con título, descripción, fecha de vencimiento y estado; en una tienda en línea, el “Producto”. Las pantallas, las validaciones y la persistencia se construyen alrededor de estas entidades, así que conviene definirlas antes de programar las funcionalidades.

---

## Definición de Entidades del Dominio

De ahora en adelante, a manera de ejemplo, trabajaremos con una aplicación donde los ciudadanos (`users`) reportan problemas urbanos usando un mapa, y los administradores (`admin`) gestionan, validan y dan seguimiento a esos reportes.

Para esta aplicación se necesitan tres entidades, que son el reporte (`Report`), su ubicación (`Location`) y el usuario (`User`), además de dos enumeraciones para el estado del reporte (`ReportStatus`) y el rol del usuario (`UserRole`).

---

## Creación de las Entidades del Dominio

En el paquete `domain.model`, cree los siguientes archivos de Kotlin para definir las entidades y enumeraciones mencionadas:

### 1. `Report.kt`
Para la entidad `Report`, cree un archivo llamado `Report.kt` y defina la clase de la siguiente manera:

```kotlin
package com.example.demoapp.domain.model

import java.time.LocalDate

data class Report(
    val id: String,
    val title: String,
    val description: String,     // Descripción detallada del problema
    val location: Location,      // Ubicación geográfica del problema
    val status: ReportStatus,
    val type: String,            // Tipo de problema: hueco en la vía, alumbrado dañado, basura, etc.
    val photoUrl: String,        // URL de la foto del problema
    val ownerId: String,         // ID del usuario que creó el reporte
    val date: LocalDate          // Fecha de creación
)
```

> `date` usa la clase `LocalDate` del paquete `java.time`; servirá, por ejemplo, para ordenar los reportes del más reciente al más antiguo.

### 2. `Location.kt`
Para la entidad `Location`, cree un archivo llamado `Location.kt` y defina la clase de la siguiente manera:

```kotlin
package com.example.demoapp.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double
)
```

### 3. `User.kt`
Para la entidad `User`, cree un archivo llamado `User.kt` y defina la clase de la siguiente manera:

```kotlin
package com.example.demoapp.domain.model

data class User (
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val email: String,
    val password: String,
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val role: UserRole = UserRole.USER
)
```

### 4. `ReportStatus.kt`
Para la enumeración `ReportStatus`, cree un archivo llamado `ReportStatus.kt` y defina la enumeración de la siguiente manera:

```kotlin
package com.example.demoapp.domain.model

enum class ReportStatus {
    PENDING,  // Pendiente de revisión por un administrador
    VERIFIED, // Verificado: el reporte fue revisado y aprobado
    REJECTED, // Rechazado: el reporte no procede
    RESOLVED, // Resuelto: el problema reportado ya fue solucionado
    DELETED   // Eliminado: borrado lógico del reporte
}
```

### 5. `UserRole.kt`
Finalmente, para la enumeración `UserRole`, cree un archivo llamado `UserRole.kt` y defina la enumeración de la siguiente manera:

```kotlin
package com.example.demoapp.domain.model

enum class UserRole {
    USER,
    ADMIN
}
```

Con base en estas definiciones, iremos integrando nuevas pantallas y composables para gestionar los reportes en la aplicación. La entidad `User` la usaremos principalmente para el registro, el inicio de sesión y el manejo de roles; el protagonista de las pantallas que construiremos será el reporte.

Las entidades se declaran como `data class` porque su función es guardar datos. Además, Kotlin genera automáticamente `equals()`, `hashCode()`, `toString()` y `copy()` a partir de sus propiedades (ver la guía de Kotlin).

---

# Múltiples Elementos en Compose

Las aplicaciones suelen mostrar colecciones de elementos en forma de listas, galerías o cuadrículas. En la aplicación de reportes, la pantalla principal será la lista de reportes.

## Composables para Múltiples Elementos

### LazyColumn y LazyRow

`LazyColumn` y `LazyRow` muestran listas componiendo solo los elementos visibles en pantalla, así que funcionan bien con listas largas. Además, incluyen desplazamiento (scroll).

```kotlin
LazyColumn {
    items(itemsList) { item ->
        Text(text = item.name)
    }
}
```

Aquí `itemsList` es una lista de objetos con un atributo `name`; `items` crea un `Text` por cada elemento.

`LazyRow` funciona igual, pero en una fila horizontal:

```kotlin
LazyRow {
    items(itemsList) { item ->
        Text(text = item.name)
    }
}
```

Para usar `LazyColumn` y `LazyRow` son necesarios los siguientes imports:

```kotlin
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
```

### Grid

Para mostrar elementos en una cuadrícula, podemos usar `LazyVerticalGrid` o `LazyHorizontalGrid`. Estos componentes permiten organizar los elementos en filas y columnas formando una cuadrícula.

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2)
) {
    items(itemsList) { item ->
        Text(text = item.name)
    }
}
```

En este ejemplo, `LazyVerticalGrid` crea una cuadrícula con dos columnas, mostrando los nombres de los elementos en la lista.

Para usarlo son necesarios los siguientes imports:

```kotlin
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
```

> ⚠️ **Importante:** Observe que la función `items` de las cuadrículas está en el paquete `lazy.grid`, distinto del de `LazyColumn` y `LazyRow` (`lazy`). Si importa la versión equivocada, el código no compila.

Para más información sobre cómo usar `LazyColumn`, `LazyRow` y `LazyVerticalGrid`, puede consultar la documentación oficial de Jetpack Compose: [Layouts en Jetpack Compose](https://developer.android.com/develop/ui/compose/lists).

---

## Representación gráfica

La siguiente imagen compara `LazyColumn`, `LazyRow` y `LazyVerticalGrid`:

![Ejemplo de LazyColumn con ListItem](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*WRUERIR_7YnQ3zInSVXEQA.jpeg)  
*Fuente: [Medium - Jetpack Compose LazyColumn](https://medium.com/@ahmetbostanciklioglu/common-mistakes-and-misuses-of-lazycolumn-in-jetpack-compose-9d2ce483e20e)*

Un `Column` o un `Row` también pueden mostrar una lista, pero componen todos los elementos aunque no estén visibles. Por eso, cuando hay cientos de elementos, la pantalla se vuelve lenta.

---

## Ejemplo: Listado de Reportes y su Detalle

Cree una pantalla llamada `ReportListScreen` que muestre una lista de reportes utilizando `LazyColumn`. Cada reporte puede representarse inicialmente mediante un `ListItem`; sin embargo, puede adaptar su presentación de acuerdo con el diseño y las necesidades de la aplicación.

### 1. Crear la pantalla de la lista de reportes

Cree un nuevo composable llamado `ReportListScreen` en el paquete `features/report/list` que muestre una lista de reportes:

```kotlin
package com.example.demoapp.features.report.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.demoapp.domain.model.Report
```