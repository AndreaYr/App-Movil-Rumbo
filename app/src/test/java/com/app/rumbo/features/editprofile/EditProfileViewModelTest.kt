package com.app.rumbo.features.editprofile

import com.app.rumbo.domain.model.MAX_BIO_LENGTH
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.UpdateProfileUseCase
import com.app.rumbo.testutil.FakeUserRepository
import com.app.rumbo.testutil.MainDispatcherRule
import com.app.rumbo.testutil.TestUsers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserRepository()

    private fun createViewModel(repo: FakeUserRepository = repository) =
        EditProfileViewModel(GetCurrentUserUseCase(repo), UpdateProfileUseCase(repo))

    @Test
    fun `el formulario inicia con los datos actuales del usuario`() {
        val state = createViewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals("MO", state.initials)
        assertEquals(TestUsers.mariana.email, state.email)
        assertEquals(TestUsers.mariana.name, state.name)
        assertEquals(TestUsers.mariana.bio, state.bio)
        assertEquals(TestUsers.mariana.city, state.city)
    }

    @Test
    fun `sin usuario deja de cargar con el formulario vacio`() {
        val state = createViewModel(FakeUserRepository(user = null)).uiState.value

        assertFalse(state.isLoading)
        assertEquals("", state.name)
    }

    @Test
    fun `la biografia se recorta al maximo`() {
        val viewModel = createViewModel()

        viewModel.onBioChange("a".repeat(MAX_BIO_LENGTH + 50))

        assertEquals(MAX_BIO_LENGTH, viewModel.uiState.value.bio.length)
        assertEquals(MAX_BIO_LENGTH, viewModel.uiState.value.bioLength)
    }

    @Test
    fun `los errores no aparecen hasta intentar guardar`() {
        val viewModel = createViewModel()

        viewModel.onNameChange("")

        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `guardar con nombre y ciudad vacios muestra los errores y no guarda`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("  ")
        viewModel.onCityChange("")

        viewModel.onSave()

        val state = viewModel.uiState.value
        assertEquals("Escribe tu nombre", state.nameError)
        assertEquals("Escribe tu ciudad base", state.cityError)
        assertFalse(state.isSaving)
        assertEquals(TestUsers.mariana, repository.observeCurrentUser().first())
    }

    @Test
    fun `guardar datos validos actualiza el usuario y avisa que se guardo`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("Ana Gómez")
        viewModel.onCityChange("Salento, Quindío")

        viewModel.onSave()

        val user = repository.observeCurrentUser().first()!!
        assertEquals("Ana Gómez", user.name)
        assertEquals("Salento, Quindío", user.city)
        assertEquals(EditProfileEvent.Saved, viewModel.events.first())
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `corregir un campo despues del error quita su mensaje`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("")
        viewModel.onSave()

        viewModel.onNameChange("Ana")

        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `el dialogo de eliminar cuenta se abre y se cierra`() {
        val viewModel = createViewModel()

        viewModel.onDeleteAccountClick()
        assertTrue(viewModel.uiState.value.showDeleteDialog)

        viewModel.onDeleteDialogDismiss()
        assertFalse(viewModel.uiState.value.showDeleteDialog)
    }

    @Test
    fun `confirmar eliminar cierra el dialogo y no cambia la cuenta`() = runTest {
        val viewModel = createViewModel()
        viewModel.onDeleteAccountClick()

        viewModel.onDeleteAccountConfirm()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertEquals(TestUsers.mariana, repository.observeCurrentUser().first())
    }
}
