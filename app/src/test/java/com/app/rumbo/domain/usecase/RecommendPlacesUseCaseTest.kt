package com.app.rumbo.domain.usecase

import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.AiRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendPlacesUseCaseTest {

    private class FakeAi(var reply: String?) : AiRepository {
        var calls = 0
        override suspend fun complete(system: String, user: String): String? {
            calls++
            return reply
        }
    }

    private val all = SamplePublications.create()
    private val verified = all.filter { it.status == PublicationStatus.VERIFIED }
    private fun useCase(ai: AiRepository) = RecommendPlacesUseCase(InMemoryPublicationRepository(), ai)

    private fun withLikes() = all.mapIndexed { i, p -> if (p.status == PublicationStatus.VERIFIED && i % 3 == 0) p.copy(interested = true) else p }
    private fun candidates(list: List<com.app.rumbo.domain.model.Publication>) =
        list.filter { it.status == PublicationStatus.VERIFIED && !it.interested && !it.visited }

    @Test
    fun `usa los ids y motivos que devuelve la IA`() = runTest {
        val list = withLikes()
        val target = candidates(list).first()
        val ai = FakeAi("""Claro: {"picks":[{"id":"${target.id}","reason":"Te gusta la naturaleza"}]} fin""")

        val result = useCase(ai).recommend(list, "es")

        assertEquals(1, result.size)
        assertEquals(target.id, result[0].publication.id)
        assertEquals("Te gusta la naturaleza", result[0].reason)
    }

    @Test
    fun `ignora ids inventados y recurre al respaldo`() = runTest {
        val list = withLikes()
        val ai = FakeAi("""{"picks":[{"id":"no-existe","reason":"x"}]}""")

        val result = useCase(ai).recommend(list, "es")

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.publication.id in candidates(list).map { c -> c.id } })
    }

    @Test
    fun `si la IA falla o responde basura usa el respaldo`() = runTest {
        val list = withLikes()
        assertTrue(useCase(FakeAi(null)).recommend(list, "es").isNotEmpty())
        assertTrue(useCase(FakeAi("no es json")).recommend(list, "es").isNotEmpty())
    }

    @Test
    fun `sin gustos no llama a la IA`() = runTest {
        val clean = all.map { it.copy(interested = false, visited = false) }
        val ai = FakeAi("""{"picks":[]}""")

        val result = useCase(ai).recommend(clean, "es")

        assertEquals(0, ai.calls)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `nunca recomienda lugares ya marcados ni no verificados`() = runTest {
        val list = withLikes()
        val result = useCase(FakeAi(null)).recommend(list, "en", limit = 20)

        assertTrue(result.all { it.publication.status == PublicationStatus.VERIFIED })
        assertTrue(result.none { it.publication.interested || it.publication.visited })
        assertTrue(verified.isNotEmpty())
    }
}
