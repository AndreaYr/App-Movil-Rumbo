# Arquitectura por capas: repositorios, casos de uso y Hilt

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Arquitectura por capas: repositorios, casos de uso y Hilt  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

En las guías anteriores trabajamos principalmente en la capa de presentación, es decir, en las pantallas, los formularios, la navegación y los ViewModels que las acompañan. Como todavía no teníamos un lugar común para la información, cada ViewModel guarda sus propios datos. Esto explica por qué un reporte creado en una pantalla no aparece en la lista de otra, ya que cada pantalla trabaja con su propia copia de la información. En esta guía los datos pasan a **repositorios** compartidos por toda la aplicación, y los ViewModels se limitan a preparar esos datos para mostrarlos en la UI.

Para lograrlo, la aplicación se organiza en una arquitectura por capas, en la que cada una tiene una responsabilidad bien definida:

* La **capa de presentación**, en la que hemos trabajado hasta ahora, está compuesta por las pantallas y los ViewModels.
* La **capa de dominio** contiene los modelos de datos, las interfaces de los repositorios y los **casos de uso** (use cases), que encapsulan cada acción de negocio de la aplicación.
* La **capa de datos** implementa los repositorios y se encarga del acceso a los datos.
* La **capa de inyección de dependencias** configura Hilt, que es quien crea los objetos y conecta las demás capas entre sí, por ejemplo, al entregarle a cada ViewModel los casos de uso que necesita.

---

## Conceptos Clave

### ViewModel

El ViewModel prepara los datos que muestra la UI y recibe los eventos que esta genera. Ya lo hemos usado en guías anteriores, pero ahora cambia una cosa importante. En lugar de guardar los datos por su cuenta, los obtendrá de los repositorios a través de los casos de uso.

### Repositorios

Un repositorio se ubica entre la lógica de la aplicación y las fuentes de datos, como la memoria, una base de datos local o un servicio web. Su principal ventaja es que quien lo usa pide los datos sin necesidad de saber de dónde vienen, lo que permite cambiar la fuente de datos sin afectar al resto de la aplicación.

Cada repositorio tiene dos partes. La **interfaz** declara las operaciones disponibles, como obtener o guardar datos, y se ubica en la capa de dominio. La **implementación** es la clase concreta que cumple esa interfaz y accede a los datos, y se ubica en la capa de datos. Un mismo repositorio puede tener varias implementaciones, por ejemplo, una para datos locales y otra para datos remotos.

### Casos de Uso

Un **caso de uso** representa una única acción de negocio de la aplicación, como iniciar sesión, registrar un usuario, observar la lista de reportes o crear un reporte. Cada caso de uso es una clase pequeña con una sola responsabilidad, que se ubica en la capa de dominio (paquete `domain/usecase`).

Los casos de uso se sitúan **entre el ViewModel y el repositorio**. En lugar de llamar directamente al repositorio, el ViewModel invoca un caso de uso, y es este el que se comunica con uno o varios repositorios u otras fuentes de datos. Con esta separación, cada clase hace una sola cosa y su nombre dice exactamente cuál (`LoginUserUseCase`, `RegisterUserUseCase`, etc.). El ViewModel se queda con la tarea de preparar el estado para la UI y deja de coordinar la lógica de negocio. Además, un mismo caso de uso puede usarse desde varios ViewModels, y cada acción de negocio puede probarse por separado.

Por convención, un caso de uso expone una única función con el operador `invoke`, lo que permite ejecutarlo como si fuera una función (por ejemplo `loginUserUseCase(email, password)` en lugar de `loginUserUseCase.invoke(email, password)`).

### Hilt en Jetpack Compose

Hilt es la biblioteca de inyección de dependencias recomendada para Android. Se encarga de crear los ViewModels, los casos de uso y los repositorios, y de entregar a cada clase las dependencias que declara en su constructor. De esta forma, no es necesario instanciar cada objeto a mano ni preocuparse por el orden en que deben crearse.

Para más información sobre Hilt, puede consultar la documentación oficial: [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android).

### KSP (Kotlin Symbol Processing)

KSP (*Kotlin Symbol Processing*) es una herramienta que genera código a partir de anotaciones durante la compilación. Hilt la utiliza para leer anotaciones como `@Inject` y `@HiltViewModel` y producir el código que crea cada clase. KSP reemplaza a Kapt, su antecesor, que era más lento porque trabajaba sobre código Java.

Para más información sobre KSP, puede consultar la documentación oficial: [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html).

---

## Diagrama de la arquitectura

El siguiente diagrama muestra las capas que se implementarán y cómo se comunican:

![Diagrama de la arquitectura MVVM con casos de uso, repositorios y Hilt](/guias-apps-moviles/res/arquitectura_mvvm.svg)

El diagrama muestra solo tres de las pantallas; el registro, la creación de reportes y las demás siguen el mismo patrón.

En el diagrama cada ViewModel usa un solo caso de uso porque así quedan las pantallas de esta guía, pero no es una regla. Un ViewModel puede recibir varios casos de uso en su constructor, y un mismo caso de uso puede inyectarse en varios ViewModels. Por ejemplo, la pantalla de `ReportDetail` seguramente usará `GetReportByIdUseCase` para cargar el reporte y `GetUserByIdUseCase` para mostrar los datos de su autor.

Para más información sobre la arquitectura por capas, puede consultar la documentación oficial de Android: [Arquitectura de la app](https://developer.android.com/topic/architecture/intro).

---

# Integración en el proyecto

Abra el proyecto de las guías anteriores y siga estos pasos.

## 1. Agregar dependencias

Agregue lo siguiente en el archivo `libs.versions.toml` para incluir Hilt y Ksp en su proyecto:

```toml
[versions]
hiltAndroid = "2.60.1"
hiltNavigationCompose = "1.4.0"
ksp = "2.3.6" # Desde KSP 2.3.0 la versión ya no va atada a la versión de Kotlin

[libraries]
androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hiltAndroid" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hiltAndroid" }

[plugins]
hilt-android = { id="com.google.dagger.hilt.android", version.ref="hiltAndroid"}
devtools-ksp = { id="com.google.devtools.ksp", version.ref="ksp" }
```

Ahora, en el archivo `build.gradle.kts` del proyecto, aplique el plugin de Hilt:

```kotlin
plugins {
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
}
```

Por último, en el archivo `build.gradle.kts` del módulo de la aplicación, agregue las siguientes dependencias:

```kotlin
plugins {
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtools.ksp)
}

dependencies {
    // --- Hilt Core ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // --- Hilt + Compose Navigation ---
    implementation(libs.androidx.hilt.navigation-compose)
}
```

No borre las dependencias que ya tiene; solo agregue las nuevas y sincronice el proyecto.

---

## 2. Configurar Hilt en la aplicación

Cree una clase que extienda de `Application` y anótela con `@HiltAndroidApp`. Esto inicializa Hilt en su aplicación. Por ejemplo, cree un archivo llamado `MyApp.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application()
```

---

## 3. Configurar el `AndroidManifest.xml`

En el archivo `AndroidManifest.xml`, asegúrese de declarar la clase de aplicación que acaba de crear:

```xml
<application
    android:name=".MyApp"
    ...>
    ...
</application>
```

Así Android usa `MyApp` al iniciar la aplicación, y con ella se inicializa Hilt.

---

## 4. Configurar la actividad principal

En su actividad principal (por ejemplo, `MainActivity`), agregue la anotación `@AndroidEntryPoint` para habilitar la inyección de dependencias en esta clase:

```kotlin
import dagger.hilt.android.AndroidEntryPoint
// Resto de imports...

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Resto del código...
}
```

---

## 5. Crear las interfaces de los Repositorios

Comencemos por el repositorio de reportes, que es el eje central de la aplicación. En el paquete `domain/repository`, cree una nueva interfaz llamada `ReportRepository` con el siguiente contenido:

```kotlin
package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.Report
import kotlinx.coroutines.flow.StateFlow

interface ReportRepository {
    val reports: StateFlow<List<Report>>
    suspend fun save(report: Report)
    suspend fun findById(id: String): Report?
}
```

Adicionalmente, necesitamos un repositorio para los usuarios, que por ahora usaremos para el registro y el inicio de sesión. En el mismo paquete, cree la interfaz `UserRepository`:

```kotlin
package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.User

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun save(user: User)
}
```