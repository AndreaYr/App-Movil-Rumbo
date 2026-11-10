package com.app.rumbo.data.repository

import android.content.Context
import android.util.Log
import androidx.core.util.AtomicFile
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.repository.SavedRouteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Guarda las rutas en un archivo JSON privado de la app, así siguen ahí al cerrarla. */
@Singleton
class FileSavedRouteRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SavedRouteRepository {

    private val file by lazy { AtomicFile(File(context.filesDir, FILE_NAME)) }
    private val json = Json {
        ignoreUnknownKeys = true
        // Rutas guardadas con un transporte que ya no existe (Jeep) se leen con el valor por defecto
        coerceInputValues = true
    }
    private val mutex = Mutex()
    private val routes = MutableStateFlow<List<SavedRoute>?>(null)

    override fun observeRoutes(): Flow<List<SavedRoute>> = flow {
        loaded()
        emitAll(routes.filterNotNull())
    }

    override suspend fun saveRoute(route: SavedRoute) {
        mutex.withLock {
            val updated = listOf(route) + loadedLocked()
            withContext(Dispatchers.IO) { write(updated) }
            routes.value = updated
        }
    }

    private suspend fun loaded(): List<SavedRoute> = mutex.withLock { loadedLocked() }

    private suspend fun loadedLocked(): List<SavedRoute> =
        routes.value ?: withContext(Dispatchers.IO) { read() }.also { routes.value = it }

    private fun read(): List<SavedRoute> = try {
        json.decodeFromString(file.readFully().decodeToString())
    } catch (_: FileNotFoundException) {
        emptyList()
    } catch (e: IOException) {
        Log.e(TAG, "No se pudieron leer las rutas guardadas", e)
        emptyList()
    } catch (e: SerializationException) {
        Log.e(TAG, "Archivo de rutas guardadas inválido", e)
        emptyList()
    }

    private fun write(routes: List<SavedRoute>) {
        val stream = file.startWrite()
        try {
            stream.write(json.encodeToString(routes).encodeToByteArray())
            file.finishWrite(stream)
        } catch (e: IOException) {
            file.failWrite(stream)
            throw e
        }
    }

    private companion object {
        const val FILE_NAME = "saved_routes.json"
        const val TAG = "SavedRoutes"
    }
}
