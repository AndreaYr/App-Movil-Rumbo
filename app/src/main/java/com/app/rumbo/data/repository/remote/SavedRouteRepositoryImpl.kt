package com.app.rumbo.data.repository.remote

import com.app.rumbo.data.remote.SavedRouteDto
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.repository.SavedRouteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las rutas del usuario en la subcolección "users/{uid}/savedRoutes" de Firestore.
 * Si no hay sesión iniciada no se lee ni se escribe nada.
 */
@Singleton
class SavedRouteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SavedRouteRepository {

    override fun observeRoutes(): Flow<List<SavedRoute>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val registration = collection(uid).addSnapshotListener { snapshot, _ ->
            val routes = snapshot?.documents
                ?.mapNotNull { it.toSavedRouteOrNull() }
                ?.sortedByDescending { it.savedAt }
                ?: emptyList()
            trySend(routes)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun saveRoute(route: SavedRoute) {
        val uid = auth.currentUser?.uid ?: return
        collection(uid).document(route.id)
            .set(SavedRouteDto.fromDomain(route))
            .await()
    }

    private fun collection(uid: String) =
        firestore.collection("users").document(uid).collection("savedRoutes")

    private fun DocumentSnapshot.toSavedRouteOrNull(): SavedRoute? = runCatching {
        toObject(SavedRouteDto::class.java)?.copy(id = id)?.toDomain()
    }.getOrNull()
}
