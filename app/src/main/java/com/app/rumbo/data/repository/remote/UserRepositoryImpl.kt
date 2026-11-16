package com.app.rumbo.data.repository.remote

import com.app.rumbo.data.remote.UserDto
import com.app.rumbo.domain.exception.EmailAlreadyInUseException
import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole
import com.app.rumbo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val collection = firestore.collection("users")

    /**
     * Observa la sesión de Firebase Authentication y, cuando hay usuario, su documento
     * en Firestore. El listener del documento mantiene el perfil actualizado en tiempo real.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentUser(): Flow<User?> =
        authStateFlow().flatMapLatest { firebaseUser ->
            if (firebaseUser == null) flowOf(null) else userDocumentFlow(firebaseUser.uid)
        }

    override suspend fun createUser(user: User): User {
        // Firebase Authentication rechaza los correos repetidos, no hace falta consultarlos antes
        val result = try {
            auth.createUserWithEmailAndPassword(user.email, user.password).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            throw EmailAlreadyInUseException()
        }
        val newUser = result.user ?: throw Exception("No se pudo obtener el UID del usuario creado")

        // El documento usa el UID de Firebase Authentication como id y nunca guarda la contraseña
        val savedUser = user.copy(id = newUser.uid, password = "")
        try {
            collection.document(newUser.uid).set(UserDto.fromDomain(savedUser)).await()
        } catch (e: Exception) {
            // Si no se pudo guardar el perfil, se borra la cuenta para no dejarla incompleta
            withContext(NonCancellable) { runCatching { newUser.delete().await() } }
            throw e
        }

        // Se conserva el flujo actual: tras registrarse se vuelve al login para iniciar sesión
        auth.signOut()
        return savedUser
    }

    override suspend fun login(email: String, password: String): User? {
        val result = try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return null // Correo o contraseña incorrectos
        } catch (e: FirebaseAuthInvalidUserException) {
            return null // La cuenta no existe o fue deshabilitada
        }

        val uid = result.user?.uid ?: return null
        val user = collection.document(uid).get().await()
            .toObject(UserDto::class.java)
            ?.copy(id = uid)
            ?.toDomain()

        // Si la cuenta existe en Authentication pero no tiene perfil, no se deja la sesión abierta
        if (user == null) {
            auth.signOut()
            return null
        }
        return user
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val firebaseUser = auth.signInWithCredential(credential).await().user
            ?: throw Exception("No se pudo obtener el usuario de Google")
        val uid = firebaseUser.uid
        val document = collection.document(uid)

        val existing = document.get().await().toObject(UserDto::class.java)?.copy(id = uid)?.toDomain()
        if (existing != null) return existing // Cuenta ya registrada: solo se inicia sesión

        // Primer ingreso con Google: se crea el perfil igual que en el registro con correo
        val newUser = User(
            id = uid,
            name = firebaseUser.displayName.orEmpty().ifBlank { firebaseUser.email.orEmpty().substringBefore('@') },
            email = firebaseUser.email.orEmpty(),
            city = "",
            levelName = "Explorador",
            role = UserRole.USER
        )
        try {
            document.set(UserDto.fromDomain(newUser)).await()
        } catch (e: Exception) {
            withContext(NonCancellable) { auth.signOut() }
            throw e
        }
        return newUser
    }

    override suspend fun sendPasswordReset(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (_: FirebaseAuthInvalidUserException) {
            // No se revela si el correo existe: se responde igual que en un envío exitoso
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun updateProfile(update: ProfileUpdate) {
        val uid = auth.currentUser?.uid ?: return
        collection.document(uid).update(
            mapOf(
                "name" to update.name,
                "bio" to update.bio,
                "city" to update.city
            )
        ).await()
    }

    private fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun userDocumentFlow(uid: String): Flow<User?> = callbackFlow {
        val registration = collection.document(uid).addSnapshotListener { snapshot, _ ->
            val user = snapshot
                ?.takeIf { it.exists() }
                ?.toObject(UserDto::class.java)
                ?.copy(id = uid)
                ?.toDomain()
            trySend(user)
        }
        awaitClose { registration.remove() }
    }
}
