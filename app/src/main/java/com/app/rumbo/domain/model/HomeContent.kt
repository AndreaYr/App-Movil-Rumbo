package com.app.rumbo.domain.model

/** Listas que alimentan los bloques de la pantalla de inicio. */
data class HomeContent(
    val nearby: List<Publication> = emptyList(),
    val topRated: List<Publication> = emptyList(),
    val trending: List<Trend> = emptyList(),
    val fresh: List<Publication> = emptyList()
)
