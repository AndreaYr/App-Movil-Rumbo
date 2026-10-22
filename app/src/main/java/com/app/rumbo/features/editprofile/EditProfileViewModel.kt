package com.app.rumbo.features.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.MAX_BIO_LENGTH
import com.app.rumbo.domain.model.ProfileField
import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.UpdateProfileResult
import com.app.rumbo.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val initials: String = "",
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val city: String = "",
    val isSaving: Boolean = false,
    val showValidationErrors: Boolean = false,
    val showDeleteDialog: Boolean = false
) {
    private val invalidFields: Set<ProfileField> get() = ProfileUpdate(name, bio, city).errors

    val nameError: String? get() = if (showValidationErrors && ProfileField.NAME in invalidFields) "Escribe tu nombre" else null
    val cityError: String? get() = if (showValidationErrors && ProfileField.CITY in invalidFields) "Escribe tu ciudad base" else null
    val bioLength: Int get() = bio.length
}

/** Avisos de una sola vez que la pantalla debe atender. */
sealed interface EditProfileEvent {
    data object Saved : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val updateProfile: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Se lee una sola vez: mientras se edita, el formulario no debe cambiar por debajo
        viewModelScope.launch {
            val user = getCurrentUser().first()
            _uiState.update {
                if (user == null) {
                    it.copy(isLoading = false)
                } else {
                    it.copy(
                        isLoading = false,
                        initials = user.initials,
                        email = user.email,
                        name = user.name,
                        bio = user.bio,
                        city = user.city
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value.take(MAX_BIO_LENGTH)) }

    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (updateProfile(ProfileUpdate(state.name, state.bio, state.city))) {
                UpdateProfileResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(EditProfileEvent.Saved)
                }
                is UpdateProfileResult.Invalid ->
                    _uiState.update { it.copy(isSaving = false, showValidationErrors = true) }
            }
        }
    }

    fun onDeleteAccountClick() = _uiState.update { it.copy(showDeleteDialog = true) }

    fun onDeleteDialogDismiss() = _uiState.update { it.copy(showDeleteDialog = false) }

    /** Aún no hay cuentas reales que borrar: por ahora confirmar solo cierra el diálogo. */
    fun onDeleteAccountConfirm() = _uiState.update { it.copy(showDeleteDialog = false) }
}
