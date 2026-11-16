package com.app.rumbo.data.repository.remote

import com.app.rumbo.data.remote.UserDto
import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.repository.AuthorRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Obtiene los datos del autor desde el documento del usuario en la colección "users". */
@Singleton
class AuthorRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AuthorRepository {

    private val collection = firestore.collection("users")

    override fun observeAuthor(id: String): Flow<Author?> = callbackFlow {
        val registration = collection.document(id).addSnapshotListener { snapshot, _ ->
            val author = snapshot
                ?.takeIf { it.exists() }
                ?.toObject(UserDto::class.java)
                ?.copy(id = id)
                ?.toDomain()
                ?.let { Author(id = it.id, name = it.name, levelName = it.levelName) }
            trySend(author)
        }
        awaitClose { registration.remove() }
    }
}
