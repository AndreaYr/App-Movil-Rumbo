package com.app.rumbo.core.component

import com.app.rumbo.domain.model.Publication

/** Marcador de una publicación, con el color y el icono de su categoría. */
fun Publication.toMapMarker() = MapMarker(
    id = id,
    latitude = latitude,
    longitude = longitude,
    color = category.color,
    icon = category.iconRes
)
