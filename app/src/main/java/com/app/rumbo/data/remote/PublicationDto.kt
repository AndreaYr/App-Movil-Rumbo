package com.app.rumbo.data.remote

import androidx.annotation.Keep
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus

// Representación de una Publication tal como se guarda en Firestore.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject().
@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class PublicationDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = Category.GASTRONOMY.name,
    val department: String = "",
    val town: String = "",
    val address: String = "",
    val schedule: String = "",
    val distanceKm: Double = 0.0,
    val price: String = PriceLevel.FREE.name,
    val status: String = PublicationStatus.PENDING.name,
    val author: AuthorDto = AuthorDto(),
    val publishedAt: Long = 0L,
    val votes: Int = 0,
    val weeklyVotes: Int = 0,
    val interested: Boolean = false,
    val visited: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photos: List<PhotoDto> = emptyList(),
    val comments: List<CommentDto> = emptyList(),
    val rejectionReason: String? = null,
    val rejectionDetail: String? = null
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio
    fun toDomain(): Publication = Publication(
        id = id,
        title = title,
        description = description,
        // runCatching evita que un valor desconocido rompa la aplicación
        category = runCatching { Category.valueOf(category) }.getOrDefault(Category.GASTRONOMY),
        department = department,
        town = town,
        address = address,
        schedule = schedule,
        distanceKm = distanceKm,
        price = runCatching { PriceLevel.valueOf(price) }.getOrDefault(PriceLevel.FREE),
        status = runCatching { PublicationStatus.valueOf(status) }.getOrDefault(PublicationStatus.PENDING),
        author = author.toDomain(),
        publishedAt = publishedAt,
        votes = votes,
        weeklyVotes = weeklyVotes,
        interested = interested,
        visited = visited,
        latitude = latitude,
        longitude = longitude,
        photos = photos.map { it.toDomain() },
        comments = comments.map { it.toDomain() },
        rejectionReason = rejectionReason,
        rejectionDetail = rejectionDetail
    )

    companion object {
        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(publication: Publication): PublicationDto = PublicationDto(
            id = publication.id,
            title = publication.title,
            description = publication.description,
            category = publication.category.name,
            department = publication.department,
            town = publication.town,
            address = publication.address,
            schedule = publication.schedule,
            distanceKm = publication.distanceKm,
            price = publication.price.name,
            status = publication.status.name,
            author = AuthorDto.fromDomain(publication.author),
            publishedAt = publication.publishedAt,
            votes = publication.votes,
            weeklyVotes = publication.weeklyVotes,
            interested = publication.interested,
            visited = publication.visited,
            latitude = publication.latitude,
            longitude = publication.longitude,
            photos = publication.photos.map { PhotoDto.fromDomain(it) },
            comments = publication.comments.map { CommentDto.fromDomain(it) },
            rejectionReason = publication.rejectionReason,
            rejectionDetail = publication.rejectionDetail
        )
    }
}
