package com.app.rumbo.domain.model

/** Respuesta del asistente: un mensaje, los lugares que sugiere y, si aplica, el orden de la ruta. */
data class AssistantReply(
    val message: String,
    val places: List<AiRecommendation> = emptyList(),
    val route: List<Publication> = emptyList()
)
