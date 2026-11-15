# Mapas con Mapbox y rutas guardadas

Registro del trabajo realizado el 2026-11-05 en la rama `main`. Los cambios aún no tienen commit.

## Objetivo

- Hacer funcionales los mapas del prototipo `Rumbo - Prototipo y pantallas.html` con Mapbox, siguiendo la [Guía 16](https://caflorezvi.github.io/guias-apps-moviles/16.mapas.html).
- Mostrar la ubicación del usuario con su permiso, dibujar marcadores y capturar el punto que toca.
- Encapsular mapas y geolocalización en un solo composable reutilizable.
- Permitir guardar rutas y consultarlas en Perfil > Mis rutas.

## Configuración

El token público de Mapbox se guarda en `local.properties`, que no se sube a Git. Cada integrante debe agregar esta línea en su copia:

```properties
MAPBOX_ACCESS_TOKEN=pk.tu_token
```

Sin token, el proyecto compila y el mapa muestra un aviso en lugar de cargarse.

Otros cambios de configuración:

- Mapbox `11.22.0`, en sus variantes `-ndk27`, y su repositorio Maven.
- Recurso `mapbox_access_token`, generado con `resValue` y `resValues = true`.
- Filtro de ABI `arm64-v8a` y `x86_64`: la app solo se instala en dispositivos de 64 bits.
- Permisos `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`.

## Componente `MapBox`

Archivo: `app/src/main/java/com/app/rumbo/core/component/MapBox.kt`

Se encarga de la cámara, los marcadores y su agrupación, la línea de ruta, los permisos y la ubicación. No conoce el dominio ni decide qué hacer con los toques: recibe `MapMarker` y avisa con `onMarkerClick`, `onMapClick` y `onUserLocation`.

Además de lo que indica la guía, incluye:

- Marcadores con el color y el icono de cada categoría.
- Marcadores numerados y línea para las rutas.
- Marcador arrastrable.
- `locateUserRequest`, para pantallas con su propio botón de ubicación.
- Encuadre automático de los marcadores.
- Manejo de gestos para usar el mapa dentro de una pantalla con scroll.

El estilo se define en la constante `MAP_STYLE`. Actualmente es `Style.OUTDOORS`.

## Pantallas

| Pantalla | Comportamiento |
|---|---|
| Explorar | Conmutador Lista/Mapa, marcadores por categoría, búsqueda local, filtros y tarjeta del lugar seleccionado |
| Detalle | Mapa de solo lectura y botón "Cómo llegar" |
| Crear publicación | El marcador se mueve al tocar el mapa o al arrastrarlo; "Usar mi ubicación" usa la posición real |
| Editar publicación | Mismo mapa de Crear, con el botón de ubicación sobre el mapa |
| Ruta generada | Mapa con paradas numeradas, datos de la ruta y orden del recorrido |
| Mis rutas | Lista de rutas guardadas y acceso para planificar una nueva |
| Ruta guardada | Muestra la ruta guardada con la misma pantalla de Ruta generada |

### Botones de Ruta generada

- **Arriba:** volver, Guardar y Compartir. Compartir está inhabilitado por ahora.
- **Abajo:** Guardar ruta y un botón de navegación que abre el recorrido en Google Maps.

## Rutas guardadas

- **Modelo:** `SavedRoute` conserva una copia de las paradas, coordenadas, distancia, tiempo y parámetros elegidos.
- **Almacenamiento:** `FileSavedRouteRepository` escribe el archivo privado `saved_routes.json`, así que las rutas siguen disponibles al cerrar la app.
- **Duplicados:** una ruta con las mismas paradas, el mismo orden y el mismo transporte no se guarda dos veces.
- **Perfil:** "Mis rutas" muestra la cantidad guardada, y las rutas guardadas alimentan "Rutas creadas" y sus logros.

## Verificación

| Prueba | Resultado |
|---|---|
| Pruebas unitarias | 123 correctas |
| Guardar con el botón inferior y ver la ruta en Mis rutas | Correcto en el emulador |
| Cerrar la app y conservar la ruta | Correcto en el emulador |
| Tocar un marcador y ver su tarjeta | Correcto en el emulador |
| Tocar el mapa y capturar las coordenadas | Correcto en el emulador |
| Pedir permiso y usar la ubicación | Correcto con la ubicación predeterminada del emulador (California); no se aplicó la ubicación simulada de Calarcá |
| Punto azul y marcador después de "Usar mi ubicación" | Sin confirmar: no se veían en la imagen oscura |
| Guardar desde el botón superior | Sin probar |
| Botón de navegación y "Cómo llegar" | Sin probar |
| Arrastrar el marcador | Sin probar |
| Editar publicación | Sin probar |

## Problemas pendientes

### Mapa oscuro en el emulador

Con la superficie predeterminada, el mapa se ve oscuro y los marcadores semitransparentes, también con el estilo Outdoors. Con `TextureView` se vio lavado, lo que indica un fallo de transparencia al dibujar.

El emulador usa gráficos por software (`lavapipe`/SwiftShader), por lo que es la causa más probable, pero **no está confirmada**: falta probar en un móvil real o con aceleración por hardware.

Para probar en el emulador: Android Studio > Device Manager > Pixel_7 > Edit > Additional settings > Graphics acceleration > Hardware, y reiniciar con Cold Boot.

### Otros pendientes

- Los botones superiores de Ruta generada pueden tapar una parada; falta aumentar el margen superior del encuadre.
- La línea de la ruta une las paradas en línea recta, no sigue las carreteras.
- Las distancias "a X km de ti" salen de los datos de ejemplo, no de la ubicación real.
- Las rutas se guardan por dispositivo, no por usuario, porque el login todavía es simulado.
- Las rutas guardadas no se pueden borrar.
- Las rutas guardadas muestran su copia de las paradas, pero el detalle de una parada depende de las publicaciones, que siguen en memoria.

### Fuera del alcance actual

- Compartir ruta.
- Filtros avanzados y selector de capas del mapa.
- Costo en pesos, tiempo por parada, tramos entre paradas y marcar paradas como visitadas.
- Mapa en Editar perfil: el catálogo del prototipo lo menciona, pero su diseño solo muestra un campo de texto.
