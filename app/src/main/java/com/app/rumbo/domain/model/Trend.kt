package com.app.rumbo.domain.model

/**
 * Publicación cuyo interés viene creciendo.
 * @param growthPercent aumento de votos de la última semana respecto a los que tenía antes.
 */
data class Trend(
    val publication: Publication,
    val growthPercent: Int
)
