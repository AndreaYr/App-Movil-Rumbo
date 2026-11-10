# Recurso de Cadenas (Strings) e Internacionalización

**Universidad del Quindío**  
**Programa de Ingeniería de Sistemas y Computación**  
**Título:** Recurso de Cadenas (Strings) e Internacionalización  
**Docente:** Carlos Andrés Florez V.  

---

## Introducción

A lo largo del desarrollo de la aplicación hemos escrito varios textos fijos ("hardcoded") directamente en el código Kotlin dentro de los composables, por ejemplo `"Iniciar sesión"`, `"Crear una cuenta"` o los mensajes de error. Aunque esto funciona rápidamente durante las fases iniciales, no es una buena práctica en el desarrollo profesional de Android.

El uso de **Recursos de Cadenas** (`strings.xml`) permite desacoplar los textos de la interfaz de usuario, facilitando:
- **Internacionalización (i18n) y Localización (l10n):** Soporte para múltiples idiomas (español, inglés, etc.).
- **Mantenimiento y consistencia:** Cambiar un texto centralizado en un solo lugar actualiza toda la app.
- **Formateo dinámico y plurales:** Inserción limpia de parámetros en cadenas de texto.

---

## El archivo `strings.xml`

En los proyectos de Android, los textos se definen en el archivo `res/values/strings.xml` mediante etiquetas `<string>`.

```xml
<resources>
    <string name="app_name">DemoApp</string>
    <string name="login_title">Iniciar Sesión</string>
    <string name="login_email_hint">Correo electrónico</string>
    <string name="login_button">Ingresar</string>
</resources>
```

---

## Uso de Recursos de Cadenas en Jetpack Compose

Jetpack Compose proporciona la función `stringResource()` para acceder a los recursos definidos en `strings.xml`.

### 1. Cadenas de texto simples

Para obtener un texto en un Composable:

```kotlin
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.demoapp.R

@Composable
fun LoginTitle() {
    Text(text = stringResource(id = R.string.login_title))
}
```

### 2. Cadenas de texto con formato dinámico

Si una cadena contiene valores dinámicos (como un nombre de usuario o un número), podemos usar formateadores en `strings.xml`:

```xml
<string name="welcome_user">Bienvenido, %1$s!</string>
<string name="report_count">Tienes %1$d reportes pendientes</string>
```

En Compose, le pasamos los argumentos directamente a `stringResource()`:

```kotlin
val userName = "Carlos"
Text(text = stringResource(id = R.string.welcome_user, userName))
```

---

## Soporte para Múltiples Idiomas (Internacionalización)

Para agregar soporte para un nuevo idioma (por ejemplo, inglés):

1. En el panel de proyecto de Android Studio, haga clic derecho en la carpeta `res` $\rightarrow$ **New** $\rightarrow$ **Android Resource File**.
2. Nombre del archivo: `strings.xml`.
3. Seleccione el cualificador **Locale** en la lista de la izquierda y agréguelo.
4. Seleccione el idioma (por ejemplo, `en: English`) y la región si aplica.
5. Se creará una estructura como esta:
   - `res/values/strings.xml` *(Idioma por defecto, p. ej. Español)*
   - `res/values-en/strings.xml` *(Traducción al Inglés)*

### Ejemplo en `res/values-en/strings.xml`:

```xml
<resources>
    <string name="app_name">DemoApp</string>
    <string name="login_title">Sign In</string>
    <string name="login_email_hint">Email address</string>
    <string name="login_button">Log In</string>
    <string name="welcome_user">Welcome, %1$s!</string>
</resources>
```

Android cambiará automáticamente entre `values` y `values-en` según la configuración de idioma del dispositivo del usuario.

---

## Formateo de Plurales (`plurals`)

Android permite manejar plurales complejos mediante la etiqueta `<plurals>` en el archivo `strings.xml`:

```xml
<resources>
    <plurals name="reports_found">
        <item quantity="one">%d reporte encontrado</item>
        <item quantity="other">%d reportes encontrados</item>
    </plurals>
</resources>
```

En Jetpack Compose, utilizamos `pluralStringResource()`:

```kotlin
import androidx.compose.ui.res.pluralStringResource

val count = 3
Text(
    text = pluralStringResource(
        id = R.plurals.reports_found,
        count = count,
        count
    )
)
```

---

## Buenas Prácticas

1. **Evitar concatenar cadenas en código:** Evita hacer `"Hola " + name`, prefiere recursos con formato `%1$s`.
2. **Nombres descriptivos para las claves:** Utiliza prefijos por pantalla o contexto (`login_title`, `home_button_register`, `error_invalid_email`).
3. **No hardcodear cadenas:** Extrae todos los textos visibles a recursos. En Android Studio puedes presionar `Alt + Enter` (o `Option + Enter` en macOS) sobre una cadena entre comillas y seleccionar **Extract string resource**.

---

## Resumen

En esta guía aprendimos a:

1. Centralizar los textos de la aplicación en `strings.xml`.
2. Consumir recursos de texto en Jetpack Compose con `stringResource()` y `pluralStringResource()`.
3. Configurar carpetas de recursos por región (`values-en/`) para internacionalizar la aplicación.
4. Aplicar buenas prácticas de formateo y nombrado para un código mantenible.