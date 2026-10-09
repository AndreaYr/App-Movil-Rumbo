package com.app.rumbo.domain.model

/**
 * Foto de una publicación.
 * @param description texto alternativo para accesibilidad y para el marcador mientras carga.
 * @param credit autor y licencia, obligatorio para fotos con licencia Creative Commons.
 */
data class Photo(
    val url: String,
    val description: String,
    val credit: String? = null
)
