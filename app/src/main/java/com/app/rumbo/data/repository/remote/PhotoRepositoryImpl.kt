package com.app.rumbo.data.repository.remote

import android.net.Uri
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.repository.PhotoRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Sube las fotos de las publicaciones a Firebase Storage. */
@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : PhotoRepository {

    override suspend fun uploadPublicationPhotos(
        publicationId: String,
        photos: List<Photo>
    ): List<Photo> = photos.map { upload(publicationId, it) }

    private suspend fun upload(publicationId: String, photo: Photo): Photo {
        // Las fotos que ya vienen de Firestore o de una URL pública no se vuelven a subir
        if (photo.url.startsWith("http://") || photo.url.startsWith("https://")) return photo
        val reference = storage.reference
            .child("publications/$publicationId/${UUID.randomUUID()}.jpg")
        reference.putFile(Uri.parse(photo.url)).await()
        val downloadUrl = reference.downloadUrl.await().toString()
        return photo.copy(url = downloadUrl)
    }
}
