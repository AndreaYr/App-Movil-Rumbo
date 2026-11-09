package com.app.rumbo.data.local

import android.content.Context
import org.json.JSONArray

/** Departamento de Colombia con todos sus municipios y ciudades. */
data class Department(val name: String, val municipalities: List<String>)

/**
 * Divisiones político-administrativas de Colombia (DANE, 33 departamentos y 1.122 municipios).
 * Los datos están en `assets/colombia_divisiones.json`, así que funcionan sin conexión.
 */
object ColombiaDivisions {
    private const val ASSET = "colombia_divisiones.json"

    fun load(context: Context): List<Department> = runCatching {
        val json = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val towns = item.getJSONArray("municipalities")
            Department(item.getString("name"), (0 until towns.length()).map(towns::getString))
        }
    }.getOrDefault(emptyList())
}
