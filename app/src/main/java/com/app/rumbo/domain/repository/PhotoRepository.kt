package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.Photo

interface PhotoRepository {
    /**
     * Sube a Firebase Storage las fotos que aún son archivos locales y devuelve las fotos
     * con la URL de descarga; las que ya tienen una URL remota se dejan igual.
     */
    suspend fun uploadPublicationPhotos(publicationId: String, photos: List<Photo>): List<Photo>
}
