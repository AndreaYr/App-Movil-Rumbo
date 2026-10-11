# 🧪 Guía de Testing - Navegación y Snackbar

## 📱 Cómo Ejecutar la Aplicación

### Prerequisitos
```bash
# Verificar Android SDK
echo $ANDROID_HOME

# Verificar Gradle
./gradlew --version

# Verificar Java
java -version
```

### Compilar
```bash
# Limpiar y compilar
./gradlew clean build

# Solo compilar Kotlin (sin instalar APK)
./gradlew :app:compileDebugKotlin

# Ver más verbose para debugging
./gradlew clean build --stacktrace
```

### Ejecutar en Emulador
```bash
# Listar dispositivos disponibles
adb devices

# Instalar y ejecutar
./gradlew installDebug
./gradlew connectedAndroidTest

# Desde Android Studio
# 1. Select -> Run -> Run 'app'
# 2. Seleccionar emulador/dispositivo
# 3. Presionar Ctrl+R para hot reload
```

---

## ✅ Testing Manual - Flujo Completo (20 minutos)

### 1️⃣ Test Onboarding → Login (2 min)

**Escenario:** Usuario nuevo abre la app

```
INICIO
  ↓
❌ No hay botón "Atrás" - CORRECTO
  ↓
[Screen: Onboarding]
  - Texto: "Bienvenido a Rumbo"
  - 3 tarjetas (Explore, Share, Comment)
  - Botón: "Continuar"
  ✓ Verifica que se vea correctamente
  ✓ Toca el botón "Continuar"
  ↓
[Screen: Login]
  - Campo usuario
  - Campo contraseña
  - Botón "Ingresar"
  ✓ Verifica que NO haya botón "Atrás" (popUpTo eliminó Onboarding)
  ✓ Intenta presionar botón atrás del dispositivo → Debe salir de la app
```

**Resultado esperado:** ✅ Transición limpia, no hay back stack

---

### 2️⃣ Test Login con Error (3 min)

**Escenario:** Usuario intenta login con credenciales inválidas

```
[Screen: Login]
  ↓
Entrada: usuario = "admin", contraseña = "wrong"
  ↓
[Toca botón "Ingresar"]
  ↓
⏳ Loading spinner (1-2 segundos)
  ↓
🔴 Snackbar rojo con mensaje: "Usuario o contraseña incorrectos"
  ✓ Snackbar aparece en la base de la pantalla
  ✓ Se oculta automáticamente después de 5 segundos
  ✓ Puede cerrar manualmente con X
```

**Resultado esperado:** ✅ Snackbar error visible, Login no cierra

---

### 3️⃣ Test Login Exitoso (3 min)

**Escenario:** Usuario login correcto

```
[Screen: Login]
  ↓
Entrada: usuario = "admin", contraseña = "admin"
  ↓
[Toca botón "Ingresar"]
  ↓
⏳ Loading spinner (1-2 segundos)
  ↓
🟢 Snackbar verde: "Bienvenido admin"
  ↓
📄 Transición automática a Feed
  ✓ Snackbar visible durante transición
  ✓ Feed se muestra con publicaciones
  ✓ Botón atrás → Debe cerrar app (no volver a Login)
```

**Resultado esperado:** ✅ Snackbar éxito, transición a Feed, sin back a Login

---

### 4️⃣ Test Register (4 min)

**Escenario:** Usuario nuevo se registra

```
[Screen: Login]
  ↓
[Toca "¿No tienes cuenta? Regístrate"]
  ↓
[Screen: Register]
  ✓ Verifica: puede volver atrás a Login
  ↓
Entrada:
  - Nombre: "Juan Pérez"
  - Email: "juan@example.com"
  - Contraseña: "Pass123!"
  - Confirmar: "Pass123!"
  - Términos: ✓ (checkbox)
  ↓
[Toca "Registrarse"]
  ↓
⏳ Loading (1-2 segundos)
  ↓
🟢 Snackbar: "Registro exitoso"
  ↓
📄 Transición automática a Login
  ✓ RegisterScreen se oculta
  ✓ LoginScreen se muestra
  ✓ 🔔 Snackbar adicional: "Registro exitoso. Por favor inicia sesión"
     (Este viene de SavedStateHandle)
```

**Resultado esperado:** ✅ Doble Snackbar, transición a Login

---

### 5️⃣ Test Register - Validación (2 min)

**Escenario:** Usuario entra datos inválidos

```
[Screen: Register]
  ↓
Intenta registrarse SIN aceptar términos
  ↓
❌ Botón "Registrarse" está deshabilitado (gris)
  ✓ No puede hacer clic
  ↓
Acepta términos + Contraseñas no coinciden
  ↓
[Toca "Registrarse"]
  ↓
🟠 Snackbar: "Las contraseñas no coinciden"
  ✓ No navega a Login
  ✓ Snackbar desaparece automáticamente
  ✓ Usuario puede corregir y intentar de nuevo
```

**Resultado esperado:** ✅ Validación correcta, Snackbar de error

---

### 6️⃣ Test Password Recovery (3 min)

**Escenario:** Usuario olvida contraseña

```
[Screen: Login]
  ↓
[Toca "¿Olvidaste tu contraseña?"]
  ↓
[Screen: PasswordRecovery]
  ✓ Verifica: puede volver atrás a Login
  ↓
Entrada: email = "admin@example.com"
  ↓
[Toca "Enviar Instrucciones"]
  ↓
⏳ Loading (1-2 segundos)
  ↓
🟢 Snackbar: "Instrucciones enviadas a tu correo"
  ↓
Entrada: email = "invalid@example.com"
  ↓
[Toca "Enviar Instrucciones"]
  ↓
🔴 Snackbar: "Email no encontrado"
  ✓ No navega
  ✓ Snackbar desaparece automáticamente
```

**Resultado esperado:** ✅ Feedback correcto según resultado

---

### 7️⃣ Test CreatePublication (3 min)

**Escenario:** Usuario logueado crea publicación

```
[Screen: Feed] (después de login exitoso)
  ↓
[Toca botón "+"]
  ↓
[Screen: CreatePublication]
  ✓ Verifica: puede volver atrás a Feed
  ↓
Entrada:
  - Título: "Mi Viaje a Machu Picchu"
  - Descripción: "Fue increíble"
  - Localización: "Perú"
  ↓
[Toca "Guardar como borrador"]
  ↓
💾 Snackbar: "Publicación guardada como borrador"
  ✓ Se queda en CreatePublication
  ✓ Puede continuar editando
  ↓
[Toca "Publicar"]
  ↓
⏳ Loading
  ↓
✅ Snackbar: "¡Publicación creada!"
  ↓
📄 Transición a Feed
  ✓ Nueva publicación visible en lista
```

**Resultado esperado:** ✅ Snackbar para ambas acciones, transición correcta

---

## 🔍 Testing de Casos Especiales

### Edge Case 1: Presionar botón rápidamente
```
[Screen: Login]
[Presiona "Ingresar" 3 veces rápido]
  ↓
✓ Solo se envía 1 request (launchSingleTop evita duplicados)
✓ Solo 1 Snackbar aparece
```

### Edge Case 2: Cambiar de pantalla durante loading
```
[Screen: Login - Loading activo]
[Toca botón atrás]
  ↓
✓ Vuelve a Onboarding? NO → Correcto (está en Back Stack)
✓ Loading se cancela
✓ No hay Snackbar huérfano
```

### Edge Case 3: Rotar pantalla durante Snackbar
```
[Screen: Register]
[Acción que genera Snackbar]
[Girar dispositivo a horizontal]
  ↓
✓ Snackbar persiste
✓ SnackbarHostState mantiene el estado
```

### Edge Case 4: Volver de Feed con botón atrás
```
[Screen: Feed]
[Toca botón atrás del dispositivo]
  ↓
✓ NO vuelve a Login (popUpTo lo eliminó)
✓ Cierra la aplicación completamente
```

---

## 📊 Matriz de Snackbar - Validación

Cada pantalla debe mostrar Snackbar en estos casos:

| Pantalla | Éxito | Error | Draft | Nota |
|----------|-------|-------|-------|------|
| Login | ✅ "Bienvenido" | ✅ "Credenciales" | - | Color verde/rojo |
| Register | ✅ "Registrado" | ✅ "Validación" | - | SavedStateHandle a Login |
| PasswordRecovery | ✅ "Enviado" | ✅ "Email no existe" | - | Sin navegación |
| CreatePublication | ✅ "Publicado" | ✅ "Error" | ✅ "Guardado" | Borrador permanece |

**Validación:** Cada Snackbar debe:
- ✅ Aparecer en la base de la pantalla
- ✅ Tener color apropiado (verde=éxito, rojo=error)
- ✅ Mostrar texto legible
- ✅ Desaparecer automáticamente o con X
- ✅ No bloquear elementos interactivos
- ✅ Respetar rotación de pantalla

---

## 🐛 Debugging

### Si no ves Snackbar
```kotlin
// Verifica en Logcat
adb logcat | grep "Snackbar"

// En el código de la pantalla:
// 1. ¿Existe SnackbarHostState?
val snackbarHostState = remember { SnackbarHostState() }

// 2. ¿Está en Scaffold?
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    ...
)

// 3. ¿LaunchedEffect es correcto?
LaunchedEffect(state.status) {
    when (state.status) {
        SUCCESS -> snackbarHostState.showSnackbar("Mensaje")
        ERROR -> snackbarHostState.showSnackbar("Error")
    }
}
```

### Si la navegación no funciona
```kotlin
// Verifica en Logcat
adb logcat | grep "Navigation"

// Asegúrate que:
// 1. Destino existe en Destinations.kt
// 2. Ruta está en AppNavHost.kt
// 3. navController está en contexto correcto
// 4. popUpTo y launchSingleTop son correctos
```

### Si el back stack está mal
```bash
# Ver stack en Logcat
adb shell dumpsys window windows | grep -E "mCurrentFocus|mFocusedApp"

# O desde Android Studio:
# Abre: Android Studio → Profiler → Focus Tab
```

---

## 🎬 Test Script Automatizado

```bash
#!/bin/bash
# test-flow.sh - Script para testing manual guiado

echo "🧪 Iniciando test de navegación..."
echo ""
echo "1. ¿Ves Onboarding con 3 tarjetas? (y/n)"
read -r response
if [[ "$response" != "y" ]]; then
    echo "❌ FALLO: Onboarding no se muestra"
    exit 1
fi

echo "✅ Onboarding correcto"
echo ""
echo "2. Presiona 'Continuar' y verifica Login sin botón atrás (y/n)"
read -r response
if [[ "$response" != "y" ]]; then
    echo "❌ FALLO: Login tiene back button o no se navegó"
    exit 1
fi

echo "✅ Navegación Onboarding → Login correcta"
echo ""
echo "3. Intenta login incorrecto. ¿Ves Snackbar rojo? (y/n)"
read -r response
if [[ "$response" != "y" ]]; then
    echo "❌ FALLO: Snackbar no se muestra"
    exit 1
fi

echo "✅ Snackbar error correcto"
echo ""
echo "4. Login con admin/admin. ¿Ves Snackbar verde y Feed? (y/n)"
read -r response
if [[ "$response" != "y" ]]; then
    echo "❌ FALLO: Snackbar éxito o Feed no se muestra"
    exit 1
fi

echo "✅ Snackbar éxito correcto"
echo ""
echo "✅ ✅ ✅ TODOS LOS TESTS PASARON ✅ ✅ ✅"
```

**Usar:**
```bash
chmod +x test-flow.sh
./test-flow.sh
```

---

## 📋 Checklist Completo de Testing

### Onboarding (5 min)
- [ ] Se muestra en inicio
- [ ] 3 tarjetas visibles
- [ ] Botón "Continuar" funciona
- [ ] No hay botón atrás
- [ ] Botón atrás del dispositivo cierra app

### Login (10 min)
- [ ] Credenciales inválidas → Snackbar rojo
- [ ] Credenciales válidas (admin/admin) → Snackbar verde
- [ ] Transición a Feed exitosa
- [ ] Botón atrás no vuelve a Onboarding
- [ ] Rotación de pantalla mantiene estado

### Register (15 min)
- [ ] Puede acceder desde Login
- [ ] Validación de campos funciona
- [ ] Checkbox de términos requerido
- [ ] Contraseñas deben coincidir
- [ ] Registro exitoso → Snackbar verde
- [ ] Transición a Login con SavedStateHandle message
- [ ] Error de registro → Snackbar rojo
- [ ] Puede volver atrás

### Password Recovery (10 min)
- [ ] Puede acceder desde Login
- [ ] Email válido → Snackbar verde
- [ ] Email inválido → Snackbar rojo
- [ ] Puede volver atrás
- [ ] Rotación mantiene estado

### CreatePublication (15 min)
- [ ] Acceder desde Feed
- [ ] Guardar borrador → Snackbar gris
- [ ] Publicar → Snackbar verde
- [ ] Error → Snackbar rojo
- [ ] Volver a Feed con botón atrás
- [ ] Transición automática tras publicar
- [ ] Publicación aparece en Feed

### Back Stack (5 min)
- [ ] Onboarding → no hay back
- [ ] Login → no hay back desde Feed
- [ ] Register → hay back a Login
- [ ] PasswordRecovery → hay back a Login
- [ ] CreatePublication → hay back a Feed
- [ ] Botón atrás del dispositivo behaves correctly

### Snackbar Visual (5 min)
- [ ] Color correcto (verde/rojo/gris)
- [ ] Posición: base de pantalla
- [ ] Desaparece automáticamente o con X
- [ ] Texto legible
- [ ] No bloquea elementos
- [ ] Persiste en rotación

---

## 📱 Dispositivos Soportados

### Emuladores
- Android 13 API 33 (mínimo recomendado)
- Android 14 API 34 (actual)
- Tablet 7" (verificar responsive)

### Físicos
- Pixel 6 o superior
- Samsung S21 o superior
- Cualquier Android 13+

### Orientaciones
- Retrato (principal)
- Apaisado (probar rotación)

---

## 🚀 Performance

### Métricas a vigilar
```
- Tiempo Onboarding → Login: < 500ms
- Tiempo Login → Feed: < 2s (con loading)
- Snackbar aparece: < 100ms
- Transiciones suaves (60 fps)
```

### Profiling
```bash
# Usar Android Studio Profiler
# 1. Conectar dispositivo/emulador
# 2. Run → Edit Configurations → Add Profiler
# 3. Hacer acciones
# 4. Ver CPU, Memoria, Frames
```

---

## 🎓 Tips de Testing

1. **Limpiar datos entre tests**
   ```bash
   adb shell pm clear com.app.rumbo
   ```

2. **Ver logs en tiempo real**
   ```bash
   adb logcat -s "Rumbo"
   ```

3. **Tomar screenshot automatizado**
   ```bash
   adb shell screencap /sdcard/screen.png
   adb pull /sdcard/screen.png
   ```

4. **Probar entrada de texto rápidamente**
   ```bash
   adb shell input text "usuario"
   adb shell input keyevent 66  # Enter
   ```

5. **Resetear app completamente**
   ```bash
   adb shell pm uninstall com.app.rumbo
   ./gradlew installDebug
   ```

---

## ✅ Testing Finalizado

Cuando todos los tests de la checklist pasen:

```bash
git log --oneline | head -10  # Verificar commits
git status                     # Debe estar limpio

# Resultado final
echo "✅ Todos los tests pasaron"
echo "✅ Navegación funcionando correctamente"
echo "✅ Snackbar mostrando mensajes"
echo "✅ Back stack correcto"
echo "✅ Listo para producción"
```

---

**Versión:** 1.0  
**Último update:** 2026-10-10  
**Estado:** ✅ Guía completa de testing
