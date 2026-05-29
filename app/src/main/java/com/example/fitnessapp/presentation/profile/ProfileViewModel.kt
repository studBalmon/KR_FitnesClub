package com.example.fitnessapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.local.ThemeDataStore
import com.example.fitnessapp.domain.model.UserProfile
import com.example.fitnessapp.domain.repository.AuthRepository
import com.example.fitnessapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val themeDataStore: ThemeDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    val isDarkTheme: StateFlow<Boolean> = themeDataStore.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            userRepository.getProfile()
                .onSuccess { _uiState.value = ProfileUiState.Success(it) }
                .onFailure { _uiState.value = ProfileUiState.Error(it.message ?: "Ошибка загрузки") }
        }
    }

    fun saveProfile(fio: String, phone: String, email: String) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            userRepository.updateProfile(fio, phone, email)
                .onSuccess {
                    _saveState.value = SaveState.Success
                    // Обновляем локальный профиль
                    val current = (_uiState.value as? ProfileUiState.Success)?.profile
                    if (current != null) {
                        _uiState.value = ProfileUiState.Success(
                            current.copy(fio = fio, phone = phone, email = email)
                        )
                    }
                }
                .onFailure { _saveState.value = SaveState.Error(it.message ?: "Ошибка сохранения") }
        }
    }

    fun saveStateSeen() { _saveState.value = SaveState.Idle }

    fun toggleTheme() {
        viewModelScope.launch { themeDataStore.setDarkTheme(!isDarkTheme.value) }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.clearToken()
            onLogout()
        }
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
