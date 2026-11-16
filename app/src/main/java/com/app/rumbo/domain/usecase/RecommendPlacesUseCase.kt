package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.AiRecommendation
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.AiRepository
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recomienda lugares según los que el usuario marcó como "Me interesa" o "Visitado".
 * La IA solo elige entre publicaciones verificadas que existen (por id), así que no puede inventar sitios.
 * Si no hay IA, no hay gustos todavía o la respuesta no sirve, usa un respaldo por categorías afines.
 */
@Singleton
class RecommendPlacesUseCase @Inject constructor(
    private val publications: PublicationRepository,
    private val ai: AiRepository
) {
    /** [language] es el código del idioma de la app ("es" o "en"); la IA escribe el motivo en ese idioma. */
    suspend operator fun invoke(language: String, limit: Int = DEFAULT_LIMIT): List<AiRecommendation> {
        val all = publications.observePublications().first()
        // Inicio y la pantalla de recomendaciones piden lo mismo: se reutiliza la respuesta mientras los gustos no cambien
        val key = Triple(language, limit, all.filter { it.interested || it.visited }.map { it.id }.toSet())
        cache?.takeIf { it.first == key }?.let { return it.second }
        return recommend(all, language, limit).also { cache = key to it }
    }

    @Volatile
    private var cache: Pair<Triple<String, Int, Set<String>>, List<AiRecommendation>>? = null

    suspend fun recommend(all: List<Publication>, language: String, limit: Int = DEFAULT_LIMIT): List<AiRecommendation> {
        val liked = all.filter { it.status == PublicationStatus.VERIFIED && (it.interested || it.visited) }
        val candidates = all
            .filter { it.status == PublicationStatus.VERIFIED && !it.interested && !it.visited }
            .sortedByDescending { it.votes }
            .take(MAX_CANDIDATES)
        if (candidates.isEmpty()) return emptyList()
        if (liked.isEmpty()) return candidates.take(limit).map { AiRecommendation(it, "") }

        val fromAi = ai.complete(system(language, limit), userPrompt(liked, candidates))
            ?.let { parse(it, candidates, limit) }
            .orEmpty()
        return fromAi.ifEmpty { fallback(liked, candidates, limit) }
    }

    private fun system(language: String, limit: Int): String {
        val lang = if (language == "en") "English" else "Spanish"
        return "You recommend tourist places in Colombia. Pick up to $limit places from the CANDIDATES list " +
            "that best match the user's liked places. Use only ids that appear in CANDIDATES. " +
            "Reply ONLY with JSON, no markdown: {\"picks\":[{\"id\":\"<id>\",\"reason\":\"<one short sentence in $lang>\"}]}"
    }

    private fun userPrompt(liked: List<Publication>, candidates: List<Publication>): String = buildString {
        appendLine("LIKED:")
        liked.forEach { appendLine(line(it)) }
        appendLine("CANDIDATES:")
        candidates.forEach { appendLine(line(it)) }
    }

    private fun line(p: Publication) =
        "- id=${p.id} | ${p.title} | category=${p.category.name} | price=${p.price.name} | town=${p.town}"

    private fun parse(text: String, candidates: List<Publication>, limit: Int): List<AiRecommendation> {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start < 0 || end <= start) return emptyList()
        val reply = runCatching { json.decodeFromString<Reply>(text.substring(start, end + 1)) }
            .getOrNull() ?: return emptyList()
        val byId = candidates.associateBy { it.id }
        return reply.picks
            .distinctBy { it.id }
            .mapNotNull { pick -> byId[pick.id]?.let { AiRecommendation(it, pick.reason.trim()) } }
            .take(limit)
    }

    /** Sin IA: lugares de las categorías que el usuario ya prefiere, los más votados primero. */
    private fun fallback(liked: List<Publication>, candidates: List<Publication>, limit: Int): List<AiRecommendation> {
        val favorite = liked.map { it.category }.toSet()
        return candidates
            .sortedByDescending { it.category in favorite }
            .take(limit)
            .map { AiRecommendation(it, "") }
    }

    @Serializable
    private data class Reply(val picks: List<Pick> = emptyList())

    @Serializable
    private data class Pick(val id: String, val reason: String = "")

    private companion object {
        const val DEFAULT_LIMIT = 8
        const val MAX_CANDIDATES = 40
        val json = Json { ignoreUnknownKeys = true }
    }
}
