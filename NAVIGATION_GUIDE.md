# Navegación de la Aplicación Rumbo

## Estructura de Navegación Tipada

La aplicación implementa **Navigation Compose con rutas tipadas** (`@Serializable`), proporcionando:
- ✅ Seguridad de tipos en tiempo de compilación
- ✅ Paso de argumentos sin dependencias de strings
- ✅ Back stack management automático

## Flujo de Pantallas

```
┌─────────────────────────────────────────────┐
│  ONBOARDING (OnboardingDestination)         │ ◄─ Start destination
│  "Bienvenido a Rumbo"                       │
│  ├─ Explora lugares                         │
│  ├─ Comparte experiencias                   │
│  └─ Deja comentarios                        │
│                    │                         │
│                    ▼                         │
│             ┌──────────────┐                │
│             │  [Continuar] │                │
│             └──────────────┘                │
└─────────────────────────────────────────────┘
                      │
                      ▼
         ┌────────────────────────┐
         │ LOGIN (LoginDestination)│
         ├─ Email / Contraseña   │
         ├─ "¿Olvidaste tu pass?"│
         ├─ "Crear cuenta"       │
         └─ Snackbar Feedback    │
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
    ┌────────┐  ┌──────────┐  ┌────────────┐
    │REGISTER│  │PASSWORD  │  │  SUCCESS   │
    │        │  │RECOVERY  │  │   → FEED   │
    │Snackbar│  │ Snackbar │  └────────────┘
    └────────┘  └──────────┘
        │             │
        └─────┬───────┘
              │ (Back Stack)
              ▼
         ┌─────────────┐
         │   LOGIN     │
         └─────────────┘
              │
              ▼
      ┌──────────────────┐
      │ FEED (Principal) │◄─ BottomBar Navigation
      ├─ Explore (🔍)   │
      ├─ Create (➕)    │
      ├─ Home (🏠)      │
      ├─ Alerts (🔔)    │
      └─ Profile (👤)   │
              │
              ▼
    ┌──────────────────────┐
    │ CREATE PUBLICATION   │
    ├─ Título             │
    ├─ Fotos              │
    ├─ Categoría          │
    ├─ Descripción        │
    └─ Guardar/Publicar   │
         (Snackbar)
              │
              ▼
    ┌──────────────────────┐
    │PUBLICATION SUBMITTED │
    ├─ Confirmación       │
    ├─ [Mis Publicaciones]│
    └─ [Ir a Inicio]      │
              │
              ▼
    ┌──────────────────────┐
    │ MY PUBLICATIONS      │
    ├─ Lista personal     │
    └─ [Detalles]         │
              │
              ▼
    ┌──────────────────────┐
    │PUBLICATION DETAIL    │
    ├─ Mapa               │
    ├─ Información        │
    ├─ Interacciones      │
    └─ Botones de Acción  │
```

## Destinos Definidos

| Destino | Ruta | Parámetros | Descripción |
|---------|------|-----------|-------------|
| `OnboardingDestination` | `/onboarding` | - | Primera pantalla de bienvenida |
| `LoginDestination` | `/login` | - | Autenticación (email/contraseña) |
| `RegisterDestination` | `/register` | - | Crear nueva cuenta |
| `PasswordRecoveryDestination` | `/passwordrecovery` | - | Recuperar contraseña |
| `FeedDestination` | `/feed` | - | Feed principal con navegación por pestañas |
| `CreatePublicationDestination` | `/create` | - | Crear publicación |
| `PublicationSubmittedDestination` | `/submitted` | `title: String` | Confirmación post-publicación |
| `MyPublicationsDestination` | `/mypublications` | - | Publicaciones del usuario |
| `PublicationDetailDestination` | `/detail/{id}` | `publicationId: String` | Detalles de publicación |

## Patrón: Snackbar para Retroalimentación

Cada formulario implementa **Snackbar** para mostrar mensajes de usuario:

### LoginScreen
- ✅ Error: "Correo o contraseña incorrectos"
- ✅ Success: "Inicio de sesión exitoso"
- ✅ Registro completado: Mensaje pasado desde RegisterDestination

### RegisterScreen
- ✅ Success: "Registro completado"
- ✅ Error: Validaciones (email, contraseña, términos)
- ✅ Al volver a login: Snackbar de éxito

### PasswordRecoveryScreen
- ✅ Success: "Enlace enviado"
- ✅ Error: "Correo no válido"
- ✅ Demo: Mostrar estado de error

### CreatePublicationScreen
- ✅ Draft saved: "Borrador guardado"
- ✅ Published: Navega a PublicationSubmittedDestination

## Gestión de State entre Pantallas

**Ejemplo: Éxito de Registro → Login**

```kotlin
// En RegisterDestination → SUCCESS
onRegisterSuccess = {
    navController.previousBackStackEntry
        ?.savedStateHandle
        ?.set(REGISTER_SUCCESS_RESULT_KEY, REGISTER_SUCCESS_MESSAGE)
    navController.popBackStack()
}

// En LoginDestination
LaunchedEffect(registrationMessage) {
    if (registrationMessage != null) {
        snackbarHostState.showSnackbar(registrationMessage)
        onRegistrationMessageConsumed()
    }
}
```

## Transiciones Principales

### Onboarding → Login
```kotlin
navController.navigate(LoginDestination) {
    popUpTo(OnboardingDestination) { inclusive = true }
    launchSingleTop = true
}
```
**Efecto**: Onboarding se elimina del back stack (no hay retorno).

### Login → Feed (Success)
```kotlin
navController.navigate(FeedDestination) {
    popUpTo<LoginDestination> { inclusive = true }
    launchSingleTop = true
}
```
**Efecto**: Elimina todas las pantallas de auth, Feed es la raíz.

### Feed → Create → PublicationSubmitted
```kotlin
navController.navigate(PublicationSubmittedDestination(title)) {
    popUpTo(CreatePublicationDestination) { inclusive = true }
}
```
**Efecto**: Cierra el formulario de creación.

## Cambios Realizados

1. **Destinations.kt**
   - ✅ Añadido: `OnboardingDestination`
   - ✅ Orden: Onboarding primero en la definición

2. **AppNavHost.kt**
   - ✅ `startDestination = OnboardingDestination`
   - ✅ Composable para Onboarding con navegación a Login
   - ✅ Import: `OnboardingRoute` desde features.onboarding

3. **Nueva Feature: Onboarding**
   - ✅ `OnboardingViewModel.kt` (Hilt Injectable)
   - ✅ `OnboardingScreen.kt` (Composable con Route y Screen)
   - ✅ Muestra 3 características de la app
   - ✅ Botón "Continuar" navega a Login sin back stack

4. **Snackbar Integration**
   - ✅ `LoginScreen` + Snackbar (success, error, registro)
   - ✅ `RegisterScreen` + Snackbar (success, error) 
   - ✅ `PasswordRecoveryScreen` + Snackbar (success, error)
   - ✅ `CreatePublicationScreen` + Snackbar (borrador, publicado)

5. **RegisterViewModel Fix**
   - ✅ Añadido: `message: String?` en `RegisterUiState`
   - ✅ Añadido: Método `onAcceptedTermsChange` (alias)
   - ✅ Register success setea `message`

## Validación y Testing

### Build
```bash
./gradlew :app:compileDebugKotlin
```

### Runtime
- Abre la app → Onboarding
- Continuar → Login
- Login exitoso → Feed (sin opción de volver)
- Register → Mensaje en Login al volver
- Crear publicación → Snackbar de éxito

---
**Última actualización**: Implementación completa de navegación tipada con Onboarding → Login.
