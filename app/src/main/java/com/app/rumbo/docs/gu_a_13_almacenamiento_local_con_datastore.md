# Almacenamiento Local con DataStore

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Almacenamiento Local con DataStore  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

En las guías anteriores logramos centralizar el manejo de datos utilizando repositorios e inyectándolos con Hilt. Sin embargo, toda la información se guarda en listas en memoria, lo que significa que al cerrar la aplicación o reiniciar el dispositivo los datos se pierden, obligando al usuario a iniciar sesión nuevamente cada vez que entra a la app.

Para resolver este problema, Android ofrece **Jetpack DataStore**, una solución de almacenamiento de datos ligera, síncrona/asíncrona y basada en Kotlin Coroutines y Flow. DataStore es el reemplazo moderno de `SharedPreferences`.

En esta guía utilizaremos **Preferences DataStore** para persistir pares clave-valor de forma asíncrona, permitiendo guardar información como la sesión activa del usuario, preferencias de tema u otras configuraciones de la aplicación.

---

## SharedPreferences vs DataStore

| Característica | SharedPreferences | DataStore (Preferences) |
| :--- | :--- | :--- |
| **API Asíncrona** | No (utiliza bloqueos síncronos en UI) | Sí (basado en Kotlin Coroutines y Flow) |
| **Manejo de errores** | No ofrece soporte nativo para errores | Sí, utiliza `Flow` y captura excepciones de I/O |
| **Seguridad de hilos** | Riesgo de llamadas bloqueantes en el hilo principal (`ANR`) | Operaciones totalmente seguras en el hilo de E/S (`Dispatchers.IO`) |
| **Migración** | Manual | Soporta migración desde `SharedPreferences` |

---

## Tipos de DataStore

1. **Preferences DataStore:** Guarda y accede a los datos mediante claves (Key-Value), similar a `SharedPreferences`. No requiere esquema previo.
2. **Proto DataStore:** Almacena datos como instancias de un objeto personalizado utilizando *Protocol Buffers*. Garantiza la seguridad de tipos (*Type-safety*) pero requiere definir un esquema.

En esta guía abordaremos **Preferences DataStore** para gestionar la sesión del usuario.

---

# Integración en el proyecto

## 1. Agregar dependencias

Agregue la versión de DataStore en `libs.versions.toml`:

```toml
[versions]
datastore = "1.1.1"

[libraries]
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación (`app`), agregue la siguiente dependencia:

```kotlin
dependencies {
    implementation(libs.androidx.datastore.preferences)
}
```

Sincronice el proyecto.

---

## 2. Creación del DataStore Manager / Preference Manager

Cree el paquete `data/local` y dentro cree el archivo `UserPreferencesRepository.kt` o `UserPreferencesManager.kt` para gestionar la lectura y escritura de la sesión:

```kotlin
package com.example.demoapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extensión para instanciar DataStore en el Contexto
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
    }

    // Obtener el ID del usuario guardado
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Obtener el Email guardado
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    // Guardar la sesión del usuario
    suspend fun saveUserSession(userId: String, email: String, token: String = "") {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_TOKEN_KEY] = token
        }
    }

    // Limpiar la sesión (Cerrar Sesión)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
```

---

## 3. Configuración en Hilt (Módulo de Inyección)

Como `UserPreferencesManager` utiliza `@Inject constructor` y `@ApplicationContext`, Hilt puede instanciarlo automáticamente. De manera opcional, si prefiere exponer la interfaz o proveer `DataStore<Preferences>` desde un módulo de Hilt en el paquete `di`, cree `DataStoreModule.kt`:

```kotlin
package com.example.demoapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
```

---

## 4. Uso en los Casos de Uso y ViewModels

### Guardar Sesión en el Login

Actualice `LoginUserUseCase.kt` para que guarde los datos de la sesión cuando la autenticación sea exitosa:

```kotlin
package com.example.demoapp.domain.usecase

import com.example.demoapp.data.local.UserPreferencesManager
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesManager: UserPreferencesManager
) {
    suspend operator fun invoke(email: String, pass: String): User? {
        val user = userRepository.findByEmail(email)
        return if (user != null && user.password == pass) {
            userPreferencesManager.saveUserSession(user.id, user.email)
            user
        } else {
            null
        }
    }
}
```

### Consultar Estado de la Sesión al Iniciar la App

Cree un nuevo caso de uso `GetSessionUseCase.kt` en `domain/usecase`:

```kotlin
package com.example.demoapp.domain.usecase

import com.example.demoapp.data.local.UserPreferencesManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    operator fun invoke(): Flow<String?> {
        return userPreferencesManager.userId
    }
}
```

### Cerrar Sesión

Cree un caso de uso `LogoutUseCase.kt` en `domain/usecase`:

```kotlin
package com.example.demoapp.domain.usecase

import com.example.demoapp.data.local.UserPreferencesManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    suspend operator fun invoke() {
        userPreferencesManager.clearSession()
    }
}
```

---

## 5. Verificar Sesión en el Splash/Main Navigation

En `AppNavigation.kt` o un `MainViewModel`, se puede observar la sesión activa para decidir si enviar al usuario directamente a la pantalla principal (`MainRoutes.Main`) o a la pantalla de bienvenida (`MainRoutes.Home`).

```kotlin
@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val userId by mainViewModel.userIdState.collectAsState(initial = null)

    // Decidir startDestination en función de si userId existe
    val startDestination = if (userId != null) MainRoutes.Main else MainRoutes.Home

    NavHost(
        navController = rememberNavController(),
        startDestination = startDestination
    ) {
        // Rutas de navegación...
    }
}
```

---

## Resumen

En esta guía aprendimos a:

1. Integrar **Preferences DataStore** como la solución recomendada de almacenamiento liviano.
2. Definir claves fuertemente tipadas (`stringPreferencesKey`, `intPreferencesKey`, etc.) para leer y guardar datos asíncronamente con `Flow`.
3. Integrar DataStore con la arquitectura por capas e inyectarlo mediante **Hilt**.
4. Gestionar el inicio y cierre de sesión persistiendo la información en el dispositivo.