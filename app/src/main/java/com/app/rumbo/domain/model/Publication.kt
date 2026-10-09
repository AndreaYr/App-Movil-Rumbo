package com.app.rumbo.domain.model

/** Publicación de un punto de interés compartida por la comunidad. */
data class Publication(
    val id: String,
    val title: String,
    val description: String,
    val category: Category,
    val town: String,
    val address: String,
    val schedule: String,
    val distanceKm: Double,
    val price: PriceLevel,
    val status: PublicationStatus,
    val author: Author,
    val publishedAt: Long,
    val votes: Int,
    val interested: Boolean,
    val visited: Boolean,
    val latitude: Double,
    val longitude: Double,
    val photos: List<Photo>,
    val comments: List<Comment>
)
