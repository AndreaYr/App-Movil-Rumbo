package com.app.rumbo.data.repository.remote

import com.app.rumbo.data.remote.NotificationDto
import com.app.rumbo.domain.model.Notification
import com.app.rumbo.domain.repository.NotificationRepository
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
 * Guarda los avisos del usuario en la subcolección "users/{uid}/notifications" de Firestore.
 * Si no hay sesión iniciada no se lee ni se escribe nada.
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotificationRepository {

    override fun observeNotifications(): Flow<List<Notification>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val registration = collection(uid).addSnapshotListener { snapshot, _ ->
            val notifications = snapshot?.documents
                ?.mapNotNull { it.toNotificationOrNull() }
                ?.sortedByDescending { it.createdAt }
                ?: emptyList()
            trySend(notifications)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun markAsRead(id: String) {
        val uid = auth.currentUser?.uid ?: return
        runCatching { collection(uid).document(id).update("isRead", true).await() }
    }

    override suspend fun markAllAsRead() {
        val uid = auth.currentUser?.uid ?: return
        val unread = collection(uid).whereEqualTo("isRead", false).get().await().documents
        if (unread.isEmpty()) return
        val batch = firestore.batch()
        unread.forEach { batch.update(it.reference, "isRead", true) }
        batch.commit().await()
    }

    private fun collection(uid: String) =
        firestore.collection("users").document(uid).collection("notifications")

    private fun DocumentSnapshot.toNotificationOrNull(): Notification? = runCatching {
        toObject(NotificationDto::class.java)?.copy(id = id)?.toDomain()
    }.getOrNull()
}
