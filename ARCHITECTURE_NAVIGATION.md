# Arquitectura de Navegación - Rumbo App

## 📐 Enfoque: Navigation Compose con Rutas Tipadas

### ¿Por qué Rutas Tipadas?

La aplicación utiliza **rutas serializables** en lugar de strings:

```kotlin
// ❌ ANTES (string-based)
navController.navigate("login")

// ✅ AHORA (type-safe)
navController.navigate(LoginDestination)
```

**Ventajas:**
- ✅ Detección de errores en tiempo de compilación
- ✅ Refactoring automático de rutas
- ✅ Autocomplete de IDE
- ✅ Argumentos tipados sin casting

### Definición de Destinos

```kotlin
// navigation/Destinations.kt

@Serializable
data object OnboardingDestination

@Serializable
data object LoginDestination

@Serializable
data class PublicationDetailDestination(val publicationId: String) {
    companion object {
        const val ARG_ID = "publicationId"
    }
}
```

**Patrón:**
- `data object` para pantallas sin parámetros
- `data class` para pantallas con argumentos
- `@Serializable` (kotlinx.serialization) para serialización automática

---

## 🗺️ Grafo de Navegación

```
START
  ↓
[OnboardingDestination]
  ├─ Route: onboarding
  ├─ Composable: OnboardingRoute() → OnboardingScreen()
  └─ onClick("Continuar")
      ↓
[LoginDestination]  ◄─ Onboarding.popUpTo(inclusive=true)
  ├─ Route: login
  ├─ Composable: LoginRoute() → LoginScreen()
  ├─ onClick("Crear cuenta") ─────→ [RegisterDestination]
  │                                  ├─ Validación de form
  │                                  ├─ Snackbar feedback
  │                                  └─ Success: popBackStack()
  │                                     Pasa mensaje via savedStateHandle
  │
  ├─ onClick("¿Olvidaste?") ──────→ [PasswordRecoveryDestination]
  │                                  ├─ Email input
  │                                  ├─ Snackbar: "Enlace enviado"
  │                                  └─ onBack: popBackStack()
  │
  └─ Login Success (email + password válidos)
      ↓
      [FeedDestination]  ◄─ popUpTo(LoginDestination, inclusive=true)
      ├─ RumboScaffold (BottomBar Navigation)
      ├─ Tab: Explore
      │   └─ FeedRoute() → FeedScreen()
      │      └─ Publication Card click
      │         ↓
      │         [PublicationDetailDestination(id)]
      │         └─ PublicationDetailRoute()
      │            └─ OnBack: popBackStack()
      │
      ├─ Tab: Create
      │   └─ CreatePublicationRoute() → CreatePublicationScreen()
      │      ├─ Event: DraftSaved → Snackbar
      │      ├─ Event: Published
      │      │   ↓
      │      │   [PublicationSubmittedDestination(title)]
      │      │   ├─ Confirmación de publicación
      │      │   └─ onClick("Mis Publicaciones")
      │      │      ↓
      │      │      [MyPublicationsDestination]
      │      │      ├─ MyPublicationsRoute()
      │      │      ├─ Publication Card click → DetailDestination
      │      │      └─ onBack: popBackStack()
      │      │
      │      └─ onClose: popBackStack()
      │
      └─ Tab: Home, Alerts, Profile (No implementado aún)

ESTADO DE BACK STACK
═════════════════════

Onboarding Screen
  → Back: NOT INCLUDED (popUpTo con inclusive=true)

Login Screen
  → Back: Authentication state (puede ir a Register, PasswordRecovery)

Feed Screen (RAÍZ POST-LOGIN)
  → Back: BLOCKED (popUpTo con inclusive=true)

Feed → Detail
  → Back Stack: [Feed, Detail]
  → Back: Feed

Feed → Create → PublicationSubmitted
  → Back Stack: [Feed, Submitted]
  → Submitted → MyPublications
     Back Stack: [Feed, MyPublications]

```

---

## 🔧 Implementación del AppNavHost

```kotlin
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = OnboardingDestination,
        modifier = modifier
    ) {
        // Cada destino es un composable<DestinationType>()
        composable<OnboardingDestination> { ... }
        composable<LoginDestination> { entry ->
            // entry.savedStateHandle para state entre pantallas
        }
        // ... más destinos
    }
}
```

### Patrón: Composable Routing

```kotlin
// Patrón 1: Destino simple sin parámetros
composable<MyDestination> {
    MyRoute(
        onNavigate = { navController.navigate(OtherDestination) },
        onBack = { navController.popBackStack() }
    )
}

// Patrón 2: Destino con argumentos
composable<PublicationDetailDestination> { backStackEntry ->
    PublicationDetailRoute(
        publicationId = backStackEntry.arguments?.getString("publicationId"),
        onBack = { navController.popBackStack() }
    )
}

// Patrón 3: Paso de state entre pantallas
composable<LoginDestination> { entry ->
    val registrationMessage by entry.savedStateHandle
        .getStateFlow<String?>(KEY, null)
        .collectAsStateWithLifecycle()
    
    LoginRoute(
        registrationMessage = registrationMessage,
        onRegistrationMessageConsumed = {
            entry.savedStateHandle[KEY] = null
        }
    )
}
```

---

## 📨 Sistema de Retroalimentación (Snackbar)

### Arquitectura

```
ViewModel                      Screen
─────────────────────────────────────────
StateFlow<UiState>
├─ status: Status
└─ message: String?
           ↓ collectAsStateWithLifecycle()
        State: value
           ↓
    LaunchedEffect(status) {
        if (status == SUCCESS) {
            snackbar.show(message)
        }
    }
           ↓
    Snackbar (UI)
```

### Ejemplo: LoginViewModel + LoginScreen

```kotlin
// ViewModel
enum class LoginStatus { IDLE, LOADING, SUCCESS, ERROR }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.IDLE,
    val message: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.LOADING) }
            delay(800)
            val success = validate(email, password)
            _uiState.update {
                it.copy(
                    status = if (success) SUCCESS else ERROR,
                    message = if (success) "Exitoso" else "Error"
                )
            }
        }
    }
}

// Screen
@Composable
fun LoginScreen(state: LoginUiState, snackbarHostState: SnackbarHostState) {
    LaunchedEffect(state.status) {
        when (state.status) {
            LoginStatus.SUCCESS, LoginStatus.ERROR -> {
                snackbarHostState.showSnackbar(state.message.orEmpty())
            }
            else -> Unit
        }
    }
    
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        // Form content
    }
}
```

### Estados Soportados

| Pantalla | IDLE | LOADING | SUCCESS | ERROR | MESSAGE |
|----------|------|---------|---------|-------|---------|
| Login | ✅ | ✅ (spinner) | ✅ (→Feed) | ✅ (snackbar) | Dinámico |
| Register | ✅ | ✅ (spinner) | ✅ (→Login con msg) | ✅ (snackbar) | Dinámico |
| PasswordRecovery | ✅ | ✅ (spinner) | ✅ (snackbar) | ✅ (snackbar) | Dinámico |
| CreatePublication | ✅ | ✅ | ✅ (→Submitted) | - | Draft/Published |

---

## 🔗 Paso de Datos Entre Pantallas

### Método 1: Arguments (Tipados)
```kotlin
// Destino
@Serializable
data class DetailDestination(val id: String)

// Navegación
navController.navigate(DetailDestination(id = "123"))

// Lectura
composable<DetailDestination> { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id")
}
```

### Método 2: SavedStateHandle (Transient)
```kotlin
// Envío
navController.previousBackStackEntry
    ?.savedStateHandle
    ?.set("MESSAGE_KEY", "Registro exitoso")
navController.popBackStack()

// Recepción
composable<LoginDestination> { entry ->
    val message by entry.savedStateHandle
        .getStateFlow<String?>("MESSAGE_KEY", null)
        .collectAsStateWithLifecycle()
    
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.show(message)
            entry.savedStateHandle["MESSAGE_KEY"] = null
        }
    }
}
```

**Ventajas de SavedStateHandle:**
- Se persiste en process death
- Automáticamente limpiable
- No requiere serialización manual
- Ideal para mensajes transitorios

---

## 🎨 PopUpTo y LaunchSingleTop

### popUpTo(destination, inclusive)
```kotlin
// Limpia el back stack hasta destination (exclusive por defecto)
navController.navigate(FeedDestination) {
    popUpTo(LoginDestination) { inclusive = false }
    // Back stack: [Login, Feed]  ← Login permanece
}

// inclusive = true: elimina incluso el destination
navController.navigate(FeedDestination) {
    popUpTo(LoginDestination) { inclusive = true }
    // Back stack: [Feed]  ← Login se elimina
}

// Uso real: Después de login exitoso
navController.navigate(FeedDestination) {
    popUpTo<LoginDestination> { inclusive = true }
    launchSingleTop = true
}
```

### launchSingleTop
```kotlin
// Evita múltiples instancias de la misma ruta
navController.navigate(FeedDestination) {
    launchSingleTop = true
}
// Si FeedDestination ya existe en top, no crea nueva instancia
```

---

## 📦 Hilt + ViewModel Injection

```kotlin
// OnboardingViewModel.kt
@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel()

// En Composable
@Composable
fun OnboardingRoute(
    onContinueClick: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()  // Auto-injection
) {
    // ViewModel automáticamente creado y scoped a Navigation Entry
}
```

**Scope:** ViewModel vive durante todo el ciclo de vida del back stack entry.

---

## 🧪 Testing la Navegación

### Unit Test
```kotlin
@Test
fun testLoginNavigatesToFeedOnSuccess() {
    // Arrange
    val navController = TestNavHostController(context)
    
    // Act
    navController.navigate(LoginDestination)
    // Simular login exitoso
    
    // Assert
    assertEquals(FeedDestination::class, navController.currentDestination)
}
```

### UI Test
```kotlin
@Test
fun testOnboardingFlowToLogin() {
    composeTestRule.onNodeWithText("Continuar").performClick()
    
    // Verify Login screen is shown
    composeTestRule.onNodeWithText("Bienvenido de vuelta")
        .assertIsDisplayed()
}
```

---

## 🚀 Próximos Pasos

1. **Deep Linking**
   ```kotlin
   composable<PublicationDetailDestination>(
       deepLinks = listOf(
           navDeepLink { uriPattern = "https://rumbo.app/publication/{publicationId}" }
       )
   ) { ... }
   ```

2. **Animaciones**
   ```kotlin
   composable<LoginDestination>(
       enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
       exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
   ) { ... }
   ```

3. **Persistence (SessionStore)**
   - Guardar estado de navegación
   - Restaurar en app restart

4. **Real Authentication**
   - Reemplazar simulación en LoginViewModel
   - Integrar con backend API
   - Persistencia de token

---

## 📚 Referencias

- [Navigation Compose Documentation](https://developer.android.com/develop/ui/compose/navigation)
- [Type-safe Navigation](https://developer.android.com/develop/ui/compose/navigation/backstack/type-safe)
- [SavedStateHandle Guide](https://developer.android.com/guide/navigation/saved-state)
- [Material Snackbar](https://developer.android.com/reference/kotlin/androidx/compose/material3/Snackbar)

---

**Versión**: 1.0  
**Última actualización**: 2026-10-10  
**Estado**: ✅ Implementado y documentado
