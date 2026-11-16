package com.app.rumbo.domain.model

/** Lugar sugerido al usuario y la frase que explica por qué encaja con sus gustos. */
data class AiRecommendation(
    val publication: Publication,
    val reason: String
)
