package com.app.rumbo.domain.usecase

import com.app.rumbo.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendPasswordResetUseCaseTest {

    private class RecordingRepository : com.app.rumbo.domain.repository.UserRepository by FakeUserRepository() {
        var sentTo: String? = null
        override suspend fun sendPasswordReset(email: String) {
            sentTo = email
        }
    }

    @Test
    fun `envia el correo sin espacios al repositorio`() = runTest {
        val repository = RecordingRepository()

        SendPasswordResetUseCase(repository)("  ana@correo.com ")

        assertEquals("ana@correo.com", repository.sentTo)
    }
}
