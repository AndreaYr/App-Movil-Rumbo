package com.app.rumbo.data.repository.remote

import android.content.Context
import android.net.Uri
import com.app.rumbo.R
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.repository.PhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sube las fotos de las publicaciones a Cloudinary (subida sin firma con un upload preset)
 * y guarda en Firestore solo la URL pública que devuelve.
 */
@Singleton
class PhotoRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PhotoRepository {

    private val client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun uploadPublicationPhotos(
        publicationId: String,
        photos: List<Photo>
    ): List<Photo> = photos.map { upload(publicationId, it) }

    private suspend fun upload(publicationId: String, photo: Photo): Photo {
        // Las fotos que ya vienen de Firestore o de una URL pública no se vuelven a subir
        if (photo.url.startsWith("http://") || photo.url.startsWith("https://")) return photo
        val cloudName = context.getString(R.string.cloudinary_cloud_name)
        val preset = context.getString(R.string.cloudinary_upload_preset)
        if (cloudName.isBlank() || preset.isBlank()) {
            throw IOException("Falta CLOUDINARY_CLOUD_NAME o CLOUDINARY_UPLOAD_PRESET en local.properties")
        }
        return withContext(Dispatchers.IO) {
            val bytes = context.contentResolver.openInputStream(Uri.parse(photo.url))
                ?.use { it.readBytes() }
                ?: throw IOException("No se pudo leer la foto seleccionada")
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", preset)
                .addFormDataPart("folder", "rumbo/publications/$publicationId")
                .addFormDataPart(
                    "file", "foto.jpg",
                    bytes.toRequestBody("image/*".toMediaType())
                )
                .build()
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw IOException("Cloudinary ${response.code}: $text")
                photo.copy(url = JSONObject(text).getString("secure_url"))
            }
        }
    }
}
