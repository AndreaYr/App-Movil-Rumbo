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
    /** Votos "Me interesa" recibidos en los últimos 7 días; base de las tendencias. */
    val weeklyVotes: Int = 0,
    val interested: Boolean,
    val visited: Boolean,
    val latitude: Double,
    val longitude: Double,
    val photos: List<Photo>,
    val comments: List<Comment>,
    /** Motivo conservado cuando la publicación es rechazada. */
    val rejectionReason: String? = null
)
