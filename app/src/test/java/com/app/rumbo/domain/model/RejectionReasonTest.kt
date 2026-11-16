package com.app.rumbo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RejectionReasonTest {

    @Test
    fun `un codigo guardado se convierte en su motivo`() {
        RejectionReason.entries.forEach {
            assertEquals(it, RejectionReason.fromCode(it.name))
        }
    }

    @Test
    fun `un texto libre de una moderacion anterior no coincide con ningun motivo`() {
        assertNull(RejectionReason.fromCode("Ubicación incorrecta"))
        assertNull(RejectionReason.fromCode(null))
    }
}
