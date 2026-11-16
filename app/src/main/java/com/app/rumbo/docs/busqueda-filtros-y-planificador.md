# Búsqueda, filtros del mapa y mejoras del planificador

Registro del trabajo realizado sobre el mapa de Explorar, Crear publicación y Planificar ruta. Complementa [mapas-y-rutas-guardadas.md](mapas-y-rutas-guardadas.md).

## 1. Búsqueda en el mapa de Explorar

- **Búsqueda local:** coincide con título, municipio, dirección y categoría de las publicaciones.
- **Búsqueda de lugares reales:** consulta Mapbox Geocoding v6 y muestra sugerencias en una lista desplegable. Al elegir una, la cámara vuela al punto (zoom 16) y se coloca un pin.
- **Detalles de uso:** botón ✕ para limpiar, tecla de búsqueda del teclado y mensaje "Sin resultados".
- **Encuadre:** el mapa también encuadra cuando hay un solo marcador.

Arquitectura (capas del proyecto):

| Capa | Piezas |
|---|---|
| Dominio | `PlaceResult`, `PlaceSearchRepository` (`search`, `reverse`), `SearchPlacesUseCase`, `ReverseGeocodeUseCase` |
| Datos | `MapboxPlaceSearchRepository` (OkHttp), enlazado en `RepositoryModule` |
| UI | `FeedMap.kt`, `FeedScreen.kt`, `FeedViewModel.kt`, parámetro `focusPoint` en `MapBox` |

## 2. Filtros del mapa

- Se quitaron las categorías sueltas del mapa. Un botón de filtros junto al buscador abre `MapFilterSheet`.
- Orden de los filtros: **Departamento → Ciudad o municipio → Categoría (varias) → Mis lugares** (Me interesa, Visitado).
- Botones **Limpiar** (aplica de inmediato) y **Aplicar**.
- Los filtros se guardan en `FeedViewModel`, así que persisten al abrir una tarjeta y volver.
- Los marcadores solo aparecen cuando hay un filtro activo o texto de búsqueda.
- Al abrir el mapa se pide el permiso de ubicación y se centra en el usuario, salvo que ya haya filtros.
- Datos de departamentos y municipios: `app/src/main/assets/colombia_divisiones.json` (DANE, datos.gov.co, 33 departamentos y 1122 municipios), cargados con `ColombiaDivisions`.
- Componente reutilizable `SelectField` para los desplegables.

## 3. Departamento y dirección al publicar

- `Publication` y `PublicationDraft` tienen el nuevo campo `department`. Los datos de ejemplo usan "Quindío".
- Crear y Editar publicación incluyen los selectores "Departamento y municipio" (opcionales).
- El filtro usa `publication.department`; si el dato es antiguo, recurre a la lista de municipios.
- **Crear publicación** tiene un campo **Dirección**:
  - escribir busca sugerencias con 400 ms de espera;
  - elegir una sugerencia mueve el marcador;
  - tocar o arrastrar el marcador detecta la dirección sola (geocodificación inversa).
- La dirección no está en Editar publicación.

## 4. Planificar ruta

### Moto en lugar de Jeep
- Orden de transportes: A pie, Bici, **Moto**, Carro.
- Moto usa 35 km/h y el modo `two-wheeler` de Google Maps.
- Las rutas guardadas con Jeep se leen como Carro (`coerceInputValues`), para no perderlas.

### Punto de inicio
- La tarjeta "Punto de inicio" es tocable ("Cambiar") y abre `StartPointPicker`, una pantalla completa con mapa y buscador.
- El punto se fija escribiendo una dirección, tocando el mapa, arrastrando el marcador o con **Mi ubicación**. **Usar este punto** confirma.
- `RoutePreferences.start` (`StartPoint`) vale `null` cuando se usa la ubicación del usuario.
- `PlanRouteUseCase` ordena las paradas y calcula el primer tramo desde ese punto (distancia haversine).
- El punto se incluye en el enlace de "Cómo llegar" y en el que se comparte.

### Compartir ruta
- El botón de compartir de Ruta generada abre una hoja con: Copiar enlace, WhatsApp, Facebook, Instagram y Correo electrónico.
- El enlace es un recorrido de Google Maps con el transporte elegido.
- Si la app no está instalada, aparece un aviso. Facebook cae a su página web para compartir.
- Archivos: `RouteSharing.kt`, `StartPointPicker.kt` (en `features/planroute`).

## 5. Integración con la internacionalización del equipo

Al unir con los textos traducibles (`strings.xml` es/en), se ajustó:

- `Transport` usa `labelRes`, con los textos nuevos `transport_moto`, `route_share` y `plan_start_chosen`; se eliminó `transport_jeep`.
- Los textos de las pantallas nuevas (filtros del mapa, dirección, hoja de compartir, selector de inicio) **siguen escritos en español dentro del código**; falta pasarlos a recursos.

## 6. Pruebas

- Pruebas unitarias nuevas o actualizadas: `SearchPlacesUseCaseTest`, `MapFiltersTest`, `PublishDepartmentTest`, `CreateAddressTest`, `PlanRouteUseCaseTest`, `PlanRouteViewModelTest`, `SaveRouteUseCaseTest`.
- Resultado: las 141 pruebas unitarias pasan y el proyecto compila.
- **Sin verificar en pantalla:** el selector de inicio, la hoja de compartir y el envío a WhatsApp, Facebook o Instagram.

## 7. Limitaciones y pendientes

- El punto de inicio no se guarda con la ruta guardada.
- Los iconos de WhatsApp, Facebook e Instagram son dibujos simplificados, no los logos oficiales.
- Facebook e Instagram aceptan poco texto desde otras apps; en Instagram solo funciona como mensaje directo.
- Departamento y municipio son opcionales y no se rellenan solos desde la dirección.
- La dirección falta en Editar publicación.
- Si la búsqueda de lugares no devuelve nada, revisar los permisos del token de Mapbox.
- Siguen los pendientes de `mapas-y-rutas-guardadas.md`: mapa oscuro en el emulador, línea de ruta recta, rutas guardadas que no se pueden borrar y botones superiores que pueden tapar una parada.
