package com.app.rumbo.features.planroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.model.StartPoint
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.RoutePlan
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport
import com.app.rumbo.domain.model.toSavedRoute
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.GetSavedRoutesUseCase
import com.app.rumbo.domain.usecase.PlanRouteUseCase
import com.app.rumbo.domain.usecase.ReverseGeocodeUseCase
import com.app.rumbo.domain.usecase.SaveRouteUseCase
import com.app.rumbo.domain.usecase.SearchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/** Ventana "Punto de inicio": mapa con buscador de direcciones. */
data class StartPickerUiState(
    val isOpen: Boolean = false,
    val query: String = "",
    val suggestions: List<PlaceResult> = emptyList(),
    /** Punto elegido hasta ahora; se confirma con el botón inferior. */
    val point: StartPoint? = null,
    /** Cuando cambia, la cámara vuela hasta allí (al elegir una dirección). */
    val focus: StartPoint? = null
)

data class PlanRouteUiState(
    val isLoading: Boolean = true,
    val city: String = "",
    val preferences: RoutePreferences = RoutePreferences(),
    val plan: RoutePlan = RoutePlan(),
    /** La ruta actual ya está en "Mis rutas". */
    val isRouteSaved: Boolean = false
)

/** Cada cambio de parámetro recalcula la ruta al instante, como en el prototipo. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanRouteViewModel @Inject constructor(
    planRoute: PlanRouteUseCase,
    getCurrentUser: GetCurrentUserUseCase,
    getSavedRoutes: GetSavedRoutesUseCase,
    private val saveRoute: SaveRouteUseCase,
    private val searchPlaces: SearchPlacesUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase
) : ViewModel() {

    private val preferences = MutableStateFlow(RoutePreferences())

    val uiState: StateFlow<PlanRouteUiState> = combine(
        preferences,
        preferences.flatMapLatest { planRoute(it) },
        getCurrentUser(),
        getSavedRoutes()
    ) { preferences, plan, user, savedRoutes ->
        val route = plan.toSavedRoute(preferences)
        PlanRouteUiState(
            isLoading = false,
            city = user?.city.orEmpty(),
            preferences = preferences,
            plan = plan,
            isRouteSaved = savedRoutes.any { it.isSameRouteAs(route) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanRouteUiState())

    private val _picker = MutableStateFlow(StartPickerUiState())
    val pickerState: StateFlow<StartPickerUiState> = _picker.asStateFlow()
    private val startQuery = MutableStateFlow("")
    private var reverseJob: Job? = null

    init {
        // Espera a que termine de escribir y descarta las búsquedas que ya no valen
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            startQuery.debounce(SEARCH_DELAY_MS).distinctUntilChanged().collectLatest { query ->
                val reference = _picker.value.point ?: preferences.value.start
                val results = searchPlaces(
                    query,
                    reference?.latitude ?: DEFAULT_LATITUDE,
                    reference?.longitude ?: DEFAULT_LONGITUDE
                )
                _picker.update { it.copy(suggestions = results) }
            }
        }
    }

    fun onOpenStartPicker() {
        _picker.value = StartPickerUiState(isOpen = true, point = preferences.value.start)
    }

    fun onCloseStartPicker() {
        reverseJob?.cancel()
        startQuery.value = ""
        _picker.value = StartPickerUiState()
    }

    fun onStartQueryChange(value: String) {
        _picker.update { it.copy(query = value, suggestions = if (value.isBlank()) emptyList() else it.suggestions) }
        startQuery.value = value
    }

    /** Elegir una dirección de la lista pone el punto en ella y lleva la cámara hasta allí. */
    fun onStartSuggestionPicked(place: PlaceResult) {
        reverseJob?.cancel()
        startQuery.value = ""
        val point = StartPoint(place.fullAddress, place.latitude, place.longitude)
        _picker.update { it.copy(query = place.fullAddress, suggestions = emptyList(), point = point, focus = point) }
    }

    /** Tocar o arrastrar en el mapa: el punto cambia y se detecta su dirección. */
    fun onStartMapPoint(latitude: Double, longitude: Double) {
        reverseJob?.cancel()
        startQuery.value = ""
        _picker.update {
            it.copy(point = StartPoint(COORDINATES_LABEL, latitude, longitude), suggestions = emptyList())
        }
        reverseJob = viewModelScope.launch {
            reverseGeocode(latitude, longitude)?.let { place ->
                _picker.update { current ->
                    // Si el usuario ya movió el punto otra vez, esta respuesta quedó vieja
                    if (current.point?.latitude != latitude || current.point.longitude != longitude) current
                    else current.copy(query = place.fullAddress, point = StartPoint(place.fullAddress, latitude, longitude))
                }
            }
        }
    }

    /** Confirma el punto elegido como inicio de la ruta. */
    fun onConfirmStart() {
        _picker.value.point?.let { point -> preferences.update { it.copy(start = point) } }
        onCloseStartPicker()
    }

    /** Vuelve a salir de la ubicación del usuario. */
    fun onUseMyLocationAsStart() {
        preferences.update { it.copy(start = null) }
        onCloseStartPicker()
    }

    fun onHoursChange(hours: Int) {
        preferences.update {
            it.copy(hours = hours.coerceIn(RoutePreferences.MIN_HOURS, RoutePreferences.MAX_HOURS))
        }
    }

    fun onBudgetChange(budget: PriceLevel) {
        preferences.update { it.copy(budget = budget) }
    }

    fun onCategoryToggle(category: Category) {
        preferences.update {
            it.copy(categories = if (category in it.categories) it.categories - category else it.categories + category)
        }
    }

    fun onTransportChange(transport: Transport) {
        preferences.update { it.copy(transport = transport) }
    }

    fun onPriorityChange(priority: RoutePriority) {
        preferences.update { it.copy(priority = priority) }
    }

    /** Guarda la ruta actual; [onResult] indica si se pudo escribir. */
    fun onSaveRoute(onResult: (Boolean) -> Unit) {
        val state = uiState.value
        viewModelScope.launch {
            val saved = try {
                saveRoute(state.plan, state.preferences)
                true
            } catch (_: IOException) {
                false
            }
            onResult(saved)
        }
    }

    private companion object {
        const val SEARCH_DELAY_MS = 400L
        const val COORDINATES_LABEL = "Punto en el mapa"
        // Centro de Calarcá, Quindío: los resultados cercanos salen primero
        const val DEFAULT_LATITUDE = 4.5339
        const val DEFAULT_LONGITUDE = -75.6811
    }
}
