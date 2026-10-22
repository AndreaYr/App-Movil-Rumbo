package com.app.rumbo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LevelProgressTest {

    @Test
    fun `sin puntos el usuario es turista y le faltan 150 para el siguiente`() {
        val progress = LevelProgress(0)

        assertEquals(Level.TOURIST, progress.level)
        assertEquals(Level.EXPLORER, progress.nextLevel)
        assertEquals(150, progress.pointsToNext)
        assertEquals(0f, progress.progress, 0.001f)
    }

    @Test
    fun `la barra avanza dentro del nivel actual`() {
        // 75 de 150 puntos: la mitad del camino de Turista a Exploradora
        assertEquals(0.5f, LevelProgress(75).progress, 0.001f)
    }

    @Test
    fun `justo antes del umbral sigue en el nivel anterior`() {
        assertEquals(Level.TOURIST, LevelProgress(149).level)
    }

    @Test
    fun `al llegar al umbral sube de nivel y la barra reinicia`() {
        val progress = LevelProgress(150)

        assertEquals(Level.EXPLORER, progress.level)
        assertEquals(Level.ADVENTURER, progress.nextLevel)
        assertEquals(350, progress.pointsToNext)
        assertEquals(0f, progress.progress, 0.001f)
    }

    @Test
    fun `cada umbral corresponde a su nivel`() {
        assertEquals(Level.ADVENTURER, LevelProgress(500).level)
        assertEquals(Level.LOCAL_AMBASSADOR, LevelProgress(1_000).level)
    }

    @Test
    fun `en el nivel maximo no hay siguiente y la barra esta llena`() {
        val progress = LevelProgress(1_000)

        assertNull(progress.nextLevel)
        assertEquals(0, progress.pointsToNext)
        assertEquals(1f, progress.progress, 0.001f)
    }

    @Test
    fun `mas puntos que el ultimo umbral no cambian el nivel maximo`() {
        val progress = LevelProgress(5_000)

        assertEquals(Level.LOCAL_AMBASSADOR, progress.level)
        assertEquals(1f, progress.progress, 0.001f)
    }

    @Test
    fun `hay exactamente cuatro niveles ordenados por puntos`() {
        assertEquals(4, Level.entries.size)
        assertEquals(Level.entries.sortedBy { it.minPoints }, Level.entries.toList())
    }
}
