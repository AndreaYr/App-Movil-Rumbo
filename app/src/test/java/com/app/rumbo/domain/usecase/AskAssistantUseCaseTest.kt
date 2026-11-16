package com.app.rumbo.domain.usecase

import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.AiRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AskAssistantUseCaseTest {

    private class FakeAi(val reply: String?) : AiRepository {
        var calls = 0
        var lastSystem = ""
        var lastUser = ""
        override suspend fun complete(system: String, user: String): String? {
            calls++
            lastSystem = system
            lastUser = user
            return reply
        }
    }

    private val all = SamplePublications.create()
    private val verified = all.filter { it.status == PublicationStatus.VERIFIED }
    private fun useCase(reply: String?, ai: FakeAi = FakeAi(reply)) = AskAssistantUseCase(InMemoryPublicationRepository(), ai)

    @Test
    fun `devuelve mensaje, lugares y la ruta en el orden que dio la IA`() = runTest {
        val (a, b) = verified.take(2)
        val json = """{"message":"Listo","picks":[{"id":"${a.id}","reason":"r1"},{"id":"${b.id}","reason":"r2"}],"route":["${b.id}","${a.id}"]}"""

        val reply = useCase(json).reply(all, "quiero naturaleza")

        assertEquals("Listo", reply.message)
        assertEquals(listOf(a.id, b.id), reply.places.map { it.publication.id })
        assertEquals(listOf(b.id, a.id), reply.route.map { it.id })
    }

    @Test
    fun `descarta ids inventados y ruta con paradas que no se muestran`() = runTest {
        val a = verified.first()
        val json = """{"message":"x","picks":[{"id":"${a.id}"},{"id":"falso"}],"route":["${a.id}","falso"]}"""

        val reply = useCase(json).reply(all, "algo")

        assertEquals(listOf(a.id), reply.places.map { it.publication.id })
        assertTrue(reply.route.isEmpty())
    }

    @Test
    fun `si la IA falla y hay coincidencias por palabras responde con esos lugares`() = runTest {
        val place = verified.first()
        val word = place.title.split(" ").maxBy { it.length }

        val reply = useCase(null).reply(all, "quiero ir a $word")

        assertTrue(reply.places.any { it.publication.id == place.id })
        assertTrue(reply.message.contains("IA"))
    }

    @Test
    fun `si la IA falla un tema ajeno no recibe lugares aunque comparta palabras con una descripcion`() = runTest {
        val reply = useCase(null).reply(all, "como te parece El presidente de colombia?")

        assertTrue(reply.places.isEmpty())
        assertTrue(reply.message.contains("Intenta de nuevo"))
    }

    @Test
    fun `si la IA falla una categoria conocida si recibe lugares de esa categoria`() = runTest {
        val reply = useCase(null).reply(all, "quiero naturaleza")

        assertTrue(reply.places.isNotEmpty())
        assertTrue(reply.places.all { it.publication.category.name == "NATURE" })
    }

    @Test
    fun `si la IA falla y no hay coincidencias pide reintentar sin inventar lugares`() = runTest {
        val reply = useCase(null).reply(all, "quiero algo diferente xyzzy")

        assertTrue(reply.places.isEmpty())
        assertTrue(reply.message.contains("Intenta de nuevo"))
    }

    @Test
    fun `un saludo se responde con el nombre sin llamar a la IA`() = runTest {
        val ai = FakeAi("""{"message":"x"}""")

        listOf("hola", "Hola!", "buenas tardes", "Hey").forEach {
            val reply = useCase(null, ai).reply(all, it, userName = "Mariana")
            assertTrue(reply.message.startsWith("¡Hola, Mariana!"))
            assertTrue(reply.message.contains("¿Qué tipo de lugares te gustaría visitar?"))
            assertTrue(reply.places.isEmpty())
        }
        assertEquals(0, ai.calls)
    }

    @Test
    fun `una frase con hola que pide algo si va a la IA`() = runTest {
        val ai = FakeAi("""{"message":"ok","picks":[]}""")

        useCase(null, ai).reply(all, "hola quiero un plan con naturaleza", userName = "Mariana")

        assertEquals(1, ai.calls)
    }

    @Test
    fun `las groserias no llegan a la IA y se responde con respeto`() = runTest {
        val ai = FakeAi("""{"message":"x"}""")

        val reply = useCase(null, ai).reply(all, "eres una MIERDA", userName = "Mariana")

        assertEquals(0, ai.calls)
        assertTrue(reply.message.contains("respetuoso"))
        assertTrue(reply.places.isEmpty())
    }

    @Test
    fun `una respuesta solo conversacional de la IA es valida`() = runTest {
        val json = """{"message":"Solo puedo ayudarte con lugares para visitar. ¿Qué te gustaría conocer?","picks":[],"route":[]}"""

        val reply = useCase(json).reply(all, "ayudame con mi tarea de matematicas")

        assertTrue(reply.message.startsWith("Solo puedo ayudarte"))
        assertTrue(reply.places.isEmpty())
    }

    @Test
    fun `las instrucciones limitan el tema y se envia el nombre y el historial`() = runTest {
        val ai = FakeAi("""{"message":"ok","picks":[]}""")
        val history = listOf(
            com.app.rumbo.domain.model.ChatTurn(false, "¿Qué te gustaría visitar?"),
            com.app.rumbo.domain.model.ChatTurn(true, "algo tranquilo")
        )

        useCase(null, ai).reply(all, "y barato", userName = "Mariana", history = history)

        assertTrue(ai.lastSystem.contains("Mariana"))
        assertTrue(ai.lastSystem.contains("anything else"))
        assertTrue(ai.lastSystem.contains("offensive"))
        assertTrue(ai.lastUser.contains("USER: algo tranquilo"))
        assertTrue(ai.lastUser.contains("NEW USER MESSAGE: y barato"))
    }
}
