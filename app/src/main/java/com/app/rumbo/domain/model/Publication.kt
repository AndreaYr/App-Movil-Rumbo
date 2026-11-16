package com.app.rumbo.domain.model

/** Publicación de un punto de interés compartida por la comunidad. */
data class Publication(
    val id: String,
    val title: String,
    val description: String,
    val category: Category,
    /** Departamento de Colombia donde está el lugar; vacío si la publicación no lo indica. */
    val department: String = "",
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
    /** Motivo y detalle que el moderador le deja al autor cuando la rechaza. */
    val rejectionReason: String? = null,
    val rejectionDetail: String? = null,
    /** Momento en que un moderador la verificó o la rechazó; null si sigue pendiente o es anterior a este dato. */
    val moderatedAt: Long? = null,
    /** Id del moderador que la verificó o la rechazó; vacío si no se registró. */
    val moderatedBy: String = ""
) {
    /** Cuándo se resolvió la moderación; para datos antiguos sin ese registro se usa la fecha de publicación. */
    val moderationTime: Long get() = moderatedAt ?: publishedAt
}
