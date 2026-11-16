package com.app.rumbo.domain.repository

/** Cliente mínimo de un modelo de lenguaje: recibe instrucciones y devuelve el texto generado. */
interface AiRepository {
    /** Respuesta del modelo, o null si no hay clave, falla la red o el modelo no responde. */
    suspend fun complete(system: String, user: String): String?
}
