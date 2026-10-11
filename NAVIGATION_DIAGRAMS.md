# 🗺️ Mapa Visual de Navegación - Rumbo App

## Grafo Completo de Estados

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          APP INITIALIZATION                                  │
│                              (MainActivity)                                   │
└──────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
                        ┌────────────────────────┐
                        │  NavHost  (AppNavHost)  │
                        │  startDestination:     │
                        │  OnboardingDestination │
                        └────────────────────────┘
                                      │
                                      ▼
        ┌─────────────────────────────────────────────────────┐
        │            SCREEN 1: ONBOARDING                     │
        │      ┌──────────────────────────────────────┐       │
        │      │  "Bienvenido a Rumbo"               │       │
        │      │                                       │       │
        │      │  🗺  Explora lugares                  │       │
        │      │  👥  Comparte experiencias          │       │
        │      │  💬  Deja comentarios                │       │
        │      │                                       │       │
        │      │      [ Continuar ]                   │       │
        │      └──────────────────────────────────────┘       │
        │                                                      │
        │  Back Stack: [Onboarding]                           │
        │  Navigation: onClick → navigate(LoginDestination)  │
        │              popUpTo(Onboarding, inclusive=true)   │
        └─────────────────────────────────────────────────────┘
                                      │
                  popUpTo(inclusive=true) → ELIMINATED
                                      │
                                      ▼
        ┌─────────────────────────────────────────────────────┐
        │            SCREEN 2: LOGIN                          │
        │      ┌──────────────────────────────────────┐       │
        │      │  "Bienvenido de vuelta"             │       │
        │      │  Correo: [____________]  ❌          │       │
        │      │  Contraseña: [________]  ❌          │       │
        │      │  [ 🔒 Iniciar sesión ]              │       │
        │      │  [ ¿Olvidaste contraseña? ]        │       │
        │      │  [ Crear cuenta ]                   │       │
        │      └──────────────────────────────────────┘       │
        │                  ┌─────────────────────┐            │
        │                  │  SNACKBAR Area      │            │
        │                  └─────────────────────┘            │
        │      Mensajes:                                      │
        │      • "Correo o contraseña incorrectos"           │
        │      • "Inicio de sesión exitoso"                 │
        │      • "Registro de prueba completado..."         │
        │                                                      │
        │  Back Stack: [Login]                               │
        │  Routes OUT:                                        │
        │    → Click "Crear cuenta" → RegisterDestination   │
        │    → Click "¿Olvidaste?" → PasswordRecoveryDest   │
        │    → Login exitoso → FeedDestination              │
        └─────────────────────────────────────────────────────┘
                    ▲           ▲                ▲
                    │           │                │
                    │      ┌────┴────┐       ┌───┴───────┐
                    │      │          │       │           │
        ┌───────────┴──┐   │    ┌─────▼───┐   ▼           ▼
        │               │   │    │         │
    [ERROR]        ┌───┴───▼──┐ │  ┌─────────────────────────────┐
 "Correo o      │  SCREEN 3:  │ │  │  SCREEN 5: PASSWORD         │
 contraseña    │  REGISTER   │ │  │  RECOVERY                   │
 incorrectos"  │  ┌─────────┐ │ │  │  ┌──────────────────────┐   │
                │  │ Nombre  │ │ │  │  │ Correo: [_______]    │   │
              [SUCCESS]│  │Email   │ │ │  │ [ Enviar enlace ]  │   │
            "Registro  │  │Pass    │ │ │  │ [ Ver error ]      │   │
            completado│  │Confirm │ │ │  └──────────────────┐─┘   │
                       │  │Términos│ │ │    Snackbar:       │     │
                       │  │[Registrar]  │ • "Enlace enviado" │     │
                       │  └─────────┘ │ │ • "Correo inválido"│     │
                       │              │ │                    │     │
                       └──────────────┘ │ Back: popBackStack │     │
                            ▲           └────────────────────┘     │
                            │                    ▲                  │
                            │                    │                  │
                            └────────────────────┘                  │
                        onRegisterSuccess:
                        previousBackStackEntry.savedStateHandle
                        .set(REGISTER_SUCCESS_KEY, MESSAGE)
                        popBackStack()
                        
                        → Login recibe mensaje en:
                          getStateFlow(REGISTER_SUCCESS_KEY, null)
                          .collectAsStateWithLifecycle()

        ┌─────────────────────────────────────────────────────┐
        │  SCREEN 4: FEED (MAIN APP - NO BACK TO LOGIN)      │
        │     ┌──────────────────────────────────────┐        │
        │     │         RUMBO FEED                   │        │
        │     │                                       │        │
        │     │    📍 Publication 1 ─────────┐       │        │
        │     │    📍 Publication 2          │       │        │
        │     │    📍 Publication 3          │       │        │
        │     └────────────────────────┬─────┘       │        │
        │                              │             │        │
        │   ┌──────────────────────────▼───────────┐ │        │
        │   │   Bottom Navigation Bar (RumboTab)   │ │        │
        │   │   [🔍 Explore] [➕ Create] [🏠 Home]│ │        │
        │   │   [🔔 Alerts]  [👤 Profile]         │ │        │
        │   └──────────────────────────────────────┘ │        │
        │                                              │        │
        │  Back Stack: [Feed] ← No vuelve a Login   │        │
        │              (popUpTo LoginDestination,     │        │
        │               inclusive=true)               │        │
        │                                              │        │
        │  Routes OUT:                                 │        │
        │  • Explore (FeedRoute - ya activa)          │        │
        │  • Create → CreatePublicationDestination    │        │
        │  • Publication Card → PublicationDetail     │        │
        │  • My Publications → MyPublicationsDestin   │        │
        └─────────────────────────────────────────────┘
                    ▲                      ▼
                    │              ┌───────────────────┐
            Success login           │ CREATE PUBLICATION│
                    │              │                   │
                    │              │ [Título________]  │
                    │              │ [+ Fotos]         │
                    │              │ [Categoría▼]      │
                    │              │ [Descripción____] │
                    │              │ [Mapa]            │
                    │              │ [ Guardar ] [Pub] │
                    │              │                   │
                    │              │ Snackbar:         │
                    │              │ • "Borrador guard"│
                    │              │ • Publicar        │
                    │              │   → PublicationSub│
                    │              │      mittedDest   │
                    │              └───────┬───────────┘
                    │                      │
                    │              ┌───────▼──────────────┐
                    │              │  PUBLICATION SUBMITTED
                    │              │  ┌────────────────┐  │
                    │              │  │ ✓ Publicado    │  │
                    │              │  │ Título: "..."  │  │
                    │              │  │                │  │
                    │              │  │ [ Mis Publ... ]│  │
                    │              │  │ [ Ir a inicio ]│  │
                    │              │  └────────────────┘  │
                    │              │    ↓         ↓       │
                    │              └──────┬───┬───────────┘
                    │                     │   │
                    │          ┌──────────┘   └──────────┐
                    │          │                         │
                    │          ▼                         ▼
        ┌───────────┴───────────────────────┐  ┌─────────────────┐
        │ MY PUBLICATIONS                    │  │ Feed (FeedDestin│
        │ ┌───────────────────────────────┐ │  │ popUpTo(Feedtop │
        │ │ Pub 1 ────────────┐            │ │  │ inclusive=true) │
        │ │ Pub 2            │            │ │  └─────────────────┘
        │ │ Pub 3            │            │ │
        │ └────────┬──────────┘            │ │
        │          │                      │ │
        │          └──────────────┬───────┘ │
        │                         │        │
        │                         ▼        │
        │         ┌───────────────────────┐│
        │         │PUBLICATION DETAIL     ││
        │         │ • Mapa                ││
        │         │ • Título              ││
        │         │ • Info                ││
        │         │ • Comentarios         ││
        │         │ [ Me interesa ]       ││
        │         │ [ Visitado ]          ││
        │         │ [ Volver ]            ││
        │         └───────────────────────┘│
        │         popBackStack() →         │
        │         Regresa a My Publ        │
        └────────────────────────────────────
                   popBackStack() →
                   Regresa a Feed
```

## Transiciones de Estado

### 1️⃣ Onboarding → Login
```
Estado Inicial: Back Stack = [Onboarding]
Acción: Click "Continuar"
Navegación: navigate(LoginDestination) { popUpTo(Onboarding, inclusive=true) }
Estado Final: Back Stack = [Login]
Efecto: NO PUEDE volver a Onboarding (back button deshabilitado)
```

### 2️⃣ Login → Register
```
Estado Inicial: Back Stack = [Login]
Acción: Click "Crear cuenta"
Navegación: navigate(RegisterDestination)
Estado Final: Back Stack = [Login, Register]
Efecto: Puede volver atrás a Login
Salida: onRegisterSuccess → previousBackStackEntry.savedStateHandle[KEY] = MSG
        → popBackStack() → Vuelve a Login con mensaje
```

### 3️⃣ Login → Feed (Success)
```
Estado Inicial: Back Stack = [Login]
Acción: Credenciales válidas + Click "Iniciar sesión"
Navegación: navigate(FeedDestination) { popUpTo(Login, inclusive=true) }
Estado Final: Back Stack = [Feed]
Efecto: NO PUEDE volver a Login (logout necesario)
Snackbar: "Inicio de sesión exitoso"
```

### 4️⃣ Feed → CreatePublication → PublicationSubmitted
```
Estado Inicial: Back Stack = [Feed]
Acción: Click en Tab "Create"
Navegación: navigate(CreatePublicationDestination) { launchSingleTop }
Estado Intermedio: Back Stack = [Feed, CreatePublication]
Acción: Click "Publicar"
Navegación: navigate(PublicationSubmittedDestination) { popUpTo(CreatePub, inclusive=true) }
Estado Final: Back Stack = [Feed, PublicationSubmitted]
Efecto: Cierra formulario de creación
```

### 5️⃣ Detail Screen
```
Estado Inicial: Back Stack = [Feed, (otros items)]
Acción: Click en Publication Card
Navegación: navigate(PublicationDetailDestination(id))
Estado Intermedio: Back Stack = [Feed, (otros), Detail]
Acción: Click "Volver"
Navegación: popBackStack()
Estado Final: Back Stack = [Feed, (otros)]
Efecto: Regresa al anterior
```

## Snackbar Timeline

```
┌─────────────────────────────────────────────────────────────────┐
│ Timeline de Snackbar en Flujo Completo                          │
└─────────────────────────────────────────────────────────────────┘

T=0  LOGIN SCREEN appears
     │
T=5  User enters invalid credentials
     ├─ LaunchedEffect(state.status == ERROR)
     ├─ snackbarHostState.showSnackbar("Correo o contraseña..") ← SHOW 1
     └─ Back Stack: [Login]

T=10 User clicks "Crear cuenta"
     │
T=15 REGISTER SCREEN appears
     │
T=20 User fills form incorrectly (missing uppercase)
     ├─ RegisterViewModel.validatePassword() → error
     └─ Back Stack: [Login, Register]

T=25 User fills correctly
     ├─ onClick(register)
     ├─ RegisterViewModel.register()
     │  ├─ _uiState.update { status = LOADING }
     │  └─ delay(800) // Simulated API call
     │
T=33 Success
     ├─ _uiState.update { status = SUCCESS, message = "Registro completado" }
     ├─ LaunchedEffect(status == SUCCESS)
     ├─ snackbarHostState.showSnackbar("Registro completado") ← SHOW 2
     ├─ onRegisterSuccess()
     │  ├─ previousBackStackEntry.savedStateHandle[KEY] = "Registro de prueba..."
     │  └─ navController.popBackStack()
     └─ Back Stack: [Login]

T=40 Back at LOGIN SCREEN
     ├─ LaunchedEffect(registrationMessage != null)
     ├─ snackbarHostState.showSnackbar("Registro de prueba completado...") ← SHOW 3
     └─ onRegistrationMessageConsumed() → set KEY = null

T=50 User enters correct credentials
     ├─ onClick(login)
     ├─ LoginViewModel.login()
     │  ├─ Validate email/password
     │  ├─ _uiState.update { status = LOADING }
     │  └─ delay(800)
     │
T=58 Success
     ├─ _uiState.update { status = SUCCESS, message = "Éxito" }
     ├─ LaunchedEffect(status == SUCCESS)
     ├─ snackbarHostState.showSnackbar("Inicio de sesión exitoso") ← SHOW 4
     ├─ onLoginSuccess()
     │  ├─ navigate(FeedDestination)
     │  └─ popUpTo(Login, inclusive=true)
     └─ Back Stack: [Feed]

T=65 FEED SCREEN appears
```

## Matriz de Visibilidad

| Pantalla | ¿Visible? | ¿Back Button? | ¿Snackbar? | ¿Bottom Bar? |
|----------|-----------|---------------|-----------|--------------|
| Onboarding | Inicial | ❌ | ❌ | ❌ |
| Login | Post-Onboarding | ✅ (a Onboarding X) | ✅ | ❌ |
| Register | De Login | ✅ | ✅ | ❌ |
| PasswordRecovery | De Login | ✅ | ✅ | ❌ |
| Feed | Post-Login | ❌ (logout necesario) | ✅ | ✅ |
| CreatePublication | De Feed | ✅ | ✅ | ✅ |
| PublicationSubmitted | De Create | ✅ | ❌ | ✅ |
| MyPublications | De Feed/Submitted | ✅ | ❌ | ✅ |
| PublicationDetail | De Feed/MyPub | ✅ | ❌ | ✅ |

---

**Versión:** 1.0  
**Última actualización:** 2026-10-10
