package com.app.rumbo.data.remote

import android.content.Context
import com.app.rumbo.R
import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.repository.PlaceSearchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Busca lugares con la API de geocodificación de Mapbox (v6), usando el token público de la app. */
@Singleton
class MapboxPlaceSearchRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PlaceSearchRepository {

    private val client = OkHttpClient.Builder()
        .callTimeout(8, TimeUnit.SECONDS)
        .build()

    override suspend fun search(query: String, nearLatitude: Double, nearLongitude: Double): List<PlaceResult> {
        val token = context.getString(R.string.mapbox_access_token)
        if (token.isBlank()) return emptyList()
        val url = "https://api.mapbox.com/search/geocode/v6/forward".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("access_token", token)
            .addQueryParameter("country", "co")
            .addQueryParameter("language", "es")
            .addQueryParameter("proximity", "$nearLongitude,$nearLatitude")
            .addQueryParameter("limit", "5")
            .build()
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    if (!response.isSuccessful) return@use emptyList()
                    parse(response.body?.string().orEmpty())
                }
            } catch (_: IOException) {
                emptyList()
            }
        }
    }

    override suspend fun reverse(latitude: Double, longitude: Double): PlaceResult? {
        val token = context.getString(R.string.mapbox_access_token)
        if (token.isBlank()) return null
        val url = "https://api.mapbox.com/search/geocode/v6/reverse".toHttpUrl().newBuilder()
            .addQueryParameter("longitude", longitude.toString())
            .addQueryParameter("latitude", latitude.toString())
            .addQueryParameter("access_token", token)
            .addQueryParameter("language", "es")
            .addQueryParameter("limit", "1")
            .build()
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    parse(response.body?.string().orEmpty()).firstOrNull()
                }
            } catch (_: IOException) {
                null
            }
        }
    }

    private fun parse(json: String): List<PlaceResult> = runCatching {
        val features = JSONObject(json).optJSONArray("features") ?: return emptyList()
        (0 until features.length()).mapNotNull { index ->
            val properties = features.getJSONObject(index).getJSONObject("properties")
            val coordinates = properties.optJSONObject("coordinates") ?: return@mapNotNull null
            PlaceResult(
                id = properties.optString("mapbox_id", index.toString()),
                name = properties.optString("name"),
                address = properties.optString("place_formatted"),
                latitude = coordinates.getDouble("latitude"),
                longitude = coordinates.getDouble("longitude")
            )
        }
    }.getOrDefault(emptyList())
}
