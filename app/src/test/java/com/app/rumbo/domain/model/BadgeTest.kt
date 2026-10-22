package com.app.rumbo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeTest {

    @Test
    fun `un logro se desbloquea al llegar a su meta`() {
        assertFalse(Badge(Achievement.WALKER, current = 9).isUnlocked)
        assertTrue(Badge(Achievement.WALKER, current = 10).isUnlocked)
    }

    @Test
    fun `el avance es la fraccion de la meta y no pasa de uno`() {
        assertEquals(0.4f, Badge(Achievement.WALKER, current = 4).progress, 0.001f)
        assertEquals(1f, Badge(Achievement.WALKER, current = 25).progress, 0.001f)
    }

    @Test
    fun `cada logro mide la cifra de actividad que le corresponde`() {
        val stats = ActivityStats(verifiedPublications = 7, visitedPlaces = 3, commentsMade = 2)

        assertEquals(7, Achievement.TEN_VERIFIED.metric(stats))
        assertEquals(3, Achievement.WALKER.metric(stats))
        assertEquals(2, Achievement.TEN_COMMENTS.metric(stats))
    }

    @Test
    fun `el catalogo tiene 14 logros con metas y puntos positivos`() {
        assertEquals(14, Achievement.entries.size)
        assertTrue(Achievement.entries.all { it.target > 0 && it.points > 0 })
    }
}
