package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Achievement
import com.app.rumbo.domain.model.ActivityStats
import com.app.rumbo.domain.model.Level
import com.app.rumbo.testutil.FakeActivityRepository
import com.app.rumbo.testutil.FakeUserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetProfileUseCaseTest {

    private val activity = FakeActivityRepository()

    private fun useCase(user: FakeUserRepository = FakeUserRepository()) = GetProfileUseCase(user, activity)

    @Test
    fun `sin actividad el usuario es turista con cero puntos y ningun logro`() = runTest {
        val profile = useCase()().first()!!

        assertEquals(0, profile.level.points)
        assertEquals(Level.TOURIST, profile.level.level)
        assertEquals(Achievement.entries.size, profile.badges.size)
        assertTrue(profile.unlockedBadges.isEmpty())
        assertEquals(14, profile.lockedBadgesCount)
    }

    @Test
    fun `primera publicacion y primera visita suman 35 puntos`() = runTest {
        activity.stats.value = ActivityStats(publications = 1, visitedPlaces = 1)

        val profile = useCase()().first()!!

        assertEquals(
            setOf(Achievement.FIRST_PUBLICATION, Achievement.FIRST_VISIT),
            profile.unlockedBadges.map { it.achievement }.toSet()
        )
        assertEquals(35, profile.level.points)
    }

    @Test
    fun `al juntar 150 puntos sube a exploradora`() = runTest {
        // Primera publicación 25 + 10 verificadas 100 + primera visita 10 + caminante 40 = 175
        activity.stats.value = ActivityStats(publications = 10, verifiedPublications = 10, visitedPlaces = 10)

        val profile = useCase()().first()!!

        assertEquals(175, profile.level.points)
        assertEquals(Level.EXPLORER, profile.level.level)
        assertEquals(Level.ADVENTURER, profile.level.nextLevel)
    }

    @Test
    fun `un logro a medias muestra su avance sin sumar puntos`() = runTest {
        activity.stats.value = ActivityStats(visitedPlaces = 4)

        val walker = useCase()().first()!!.badges.first { it.achievement == Achievement.WALKER }

        assertFalse(walker.isUnlocked)
        assertEquals(4, walker.current)
        assertEquals(0.4f, walker.progress, 0.001f)
    }

    @Test
    fun `los contadores del perfil salen de la actividad`() = runTest {
        activity.stats.value = ActivityStats(
            publications = 5,
            pendingPublications = 2,
            visitedPlaces = 3,
            routesCreated = 1
        )

        val profile = useCase()().first()!!

        assertEquals(5, profile.publicationsCount)
        assertEquals(2, profile.pendingPublicationsCount)
        assertEquals(3, profile.visitedCount)
        assertEquals(1, profile.routesCount)
    }

    @Test
    fun `los logros de rutas se desbloquean cuando existe esa actividad`() = runTest {
        activity.stats.value = ActivityStats(routesCreated = 1)

        val unlocked = useCase()().first()!!.unlockedBadges.map { it.achievement }

        assertEquals(listOf(Achievement.ROUTE_CREATOR), unlocked)
    }

    @Test
    fun `sin usuario con sesion no hay perfil`() = runTest {
        assertNull(useCase(FakeUserRepository(user = null))().first())
    }
}
