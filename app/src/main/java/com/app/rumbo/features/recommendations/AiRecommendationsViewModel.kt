package com.app.rumbo.features.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.AiRecommendation
import com.app.rumbo.domain.model.ChatTurn
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport
import com.app.rumbo.domain.usecase.AskAssistantUseCase
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.PlanRouteUseCase
import com.app.rumbo.domain.usecase.RecommendPlacesUseCase
import com.app.rumbo.domain.usecase.SaveRouteUseCase
import com.app.rumbo.features.home.AI_LANGUAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/** Un mensaje de la conversación; los de la IA pueden traer lugares y una ruta sugerida. */
data class ChatMessage(
    val id: Int,
    val fromUser: Boolean,
    val text: String,
    val places: List<AiRecommendation> = emptyList(),
    val route: List<Publication> = emptyList()
)

data class AiRecommendationsUiState(
    val userName: String = "",
    val city: String = "",
    val isLoading: Boolean = true,
    val messages: List<ChatMessage> = emptyList(),
    /** La IA está preparando una respuesta. */
    val isThinking: Boolean = false,
    /** Mensajes cuya ruta sugerida ya se guardó en Mis rutas. */
    val savedRouteMessageIds: Set<Int> = emptySet()
)

private data class Conversation(
    val loaded: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val thinking: Boolean = false,
    val savedRouteMessageIds: Set<Int> = emptySet()
)

@HiltViewModel
class AiRecommendationsViewModel @Inject constructor(
    getCurrentUser: GetCurrentUserUseCase,
    private val recommendPlaces: RecommendPlacesUseCase,
    private val askAssistant: AskAssistantUseCase,
    private val planRoute: PlanRouteUseCase,
    private val saveRoute: SaveRouteUseCase
) : ViewModel() {

    private val conversation = MutableStateFlow(Conversation())

    val uiState: StateFlow<AiRecommendationsUiState> = combine(getCurrentUser(), conversation) { user, chat ->
        AiRecommendationsUiState(
            userName = user?.firstName.orEmpty(),
            city = user?.city.orEmpty(),
            isLoading = !chat.loaded,
            messages = chat.messages,
            isThinking = chat.thinking,
            savedRouteMessageIds = chat.savedRouteMessageIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiRecommendationsUiState())

    init {
        viewModelScope.launch {
            val picks = recommendPlaces(AI_LANGUAGE)
            val intro = if (picks.isEmpty()) {
                "Cuéntame qué te gustaría visitar con el botón del robot y te armo una ruta."
            } else {
                picks.firstOrNull { it.reason.isNotBlank() }?.reason
                    ?: "Elegí estos lugares según los que te interesan y has visitado."
            }
            conversation.value = Conversation(
                loaded = true,
                messages = listOf(ChatMessage(id = 0, fromUser = false, text = intro, places = picks))
            )
        }
    }

    /** Envía a la IA lo que el usuario quiere visitar y agrega su respuesta con lugares y ruta sugerida. */
    fun send(text: String) {
        val request = text.trim()
        if (request.isEmpty() || conversation.value.thinking) return
        val history = conversation.value.messages.map { ChatTurn(it.fromUser, it.text) }
        val userName = uiState.value.userName
        conversation.update {
            it.copy(messages = it.messages + ChatMessage(it.messages.size, fromUser = true, text = request), thinking = true)
        }
        viewModelScope.launch {
            val reply = askAssistant(request, userName, history)
            conversation.update {
                it.copy(
                    messages = it.messages + ChatMessage(it.messages.size, false, reply.message, reply.places, reply.route),
                    thinking = false
                )
            }
        }
    }

    /** Guarda en Mis rutas la ruta sugerida de ese mensaje; [onResult] indica si se pudo escribir. */
    fun saveSuggestedRoute(messageId: Int, onResult: (Boolean) -> Unit) {
        val stops = conversation.value.messages.firstOrNull { it.id == messageId }?.route.orEmpty()
        if (stops.isEmpty() || messageId in conversation.value.savedRouteMessageIds) return
        // La IA no define transporte ni horas: se deducen de las paradas elegidas
        val base = RoutePreferences(
            transport = Transport.CAR,
            priority = RoutePriority.NEAREST,
            budget = stops.maxOf { it.price },
            categories = stops.map { it.category }.toSet()
        )
        val plan = planRoute.planInOrder(stops, base)
        val hours = ((plan.totalMinutes + 59) / 60).coerceIn(RoutePreferences.MIN_HOURS, RoutePreferences.MAX_HOURS)
        viewModelScope.launch {
            val saved = try {
                saveRoute(plan, base.copy(hours = hours))
                true
            } catch (_: IOException) {
                false
            }
            if (saved) conversation.update { it.copy(savedRouteMessageIds = it.savedRouteMessageIds + messageId) }
            onResult(saved)
        }
    }
}
