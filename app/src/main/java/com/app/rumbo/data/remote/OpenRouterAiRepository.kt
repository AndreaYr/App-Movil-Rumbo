package com.app.rumbo.data.remote

import android.content.Context
import com.app.rumbo.R
import com.app.rumbo.domain.repository.AiRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Habla con el modelo gratuito de OpenRouter mediante su API compatible con OpenAI. */
@Singleton
class OpenRouterAiRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AiRepository {

    private val client = OkHttpClient.Builder()
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    override suspend fun complete(system: String, user: String): String? {
        val key = context.getString(R.string.openrouter_api_key)
        if (key.isBlank()) return null
        // Los modelos gratuitos se saturan seguido (error 429): si uno falla se prueba el siguiente
        repeat(ROUNDS) { round ->
            if (round > 0) delay(RETRY_DELAY_MS)
            for (model in MODELS) {
                request(key, model, system, user)?.let { return it }
            }
        }
        return null
    }

    private suspend fun request(key: String, model: String, system: String, user: String): String? {
        val body = JSONObject()
            .put("model", model)
            .put("temperature", 0.3)
            .put("max_tokens", 2000)
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", system))
                    .put(JSONObject().put("role", "user").put("content", user))
            )
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(ENDPOINT)
            .header("Authorization", "Bearer $key")
            .header("X-Title", "Rumbo")
            .post(body)
            .build()
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "$model respondió HTTP ${response.code}")
                        return@use null
                    }
                    runCatching {
                        JSONObject(response.body?.string().orEmpty())
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                    }.getOrNull()?.takeIf { it.isNotBlank() }
                }
            } catch (e: IOException) {
                Log.w(TAG, "$model sin conexión: ${e.javaClass.simpleName}")
                null
            }
        }
    }

    private companion object {
        const val TAG = "RumboAI"
        /** Si todos los modelos gratuitos están saturados, se reintenta una vez tras una breve pausa. */
        const val ROUNDS = 2
        const val RETRY_DELAY_MS = 2_000L
        const val ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"

        /**
         * Único lugar donde se definen los modelos, en orden de preferencia: si OpenRouter retira uno, se cambia aquí.
         * Son de proveedores distintos para que el límite de uno no deje a la app sin IA.
         */
        val MODELS = listOf(
            "google/gemma-4-26b-a4b-it:free",
            "inclusionai/ling-3.2-flash:free",
            "nvidia/nemotron-3.5-super-vl:free"
        )
    }
}
