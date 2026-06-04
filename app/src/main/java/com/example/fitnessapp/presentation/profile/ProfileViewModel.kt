package com.example.fitnessapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.local.ThemeDataStore
import com.example.fitnessapp.domain.model.UserProfile
import com.example.fitnessapp.domain.repository.AdminRepository
import com.example.fitnessapp.domain.repository.AuthRepository
import com.example.fitnessapp.domain.repository.UserRepository
import com.example.fitnessapp.presentation.theme.AccentColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
    private val adminRepository: AdminRepository,
    private val themeDataStore: ThemeDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private val _testDataPresent = MutableStateFlow<Boolean?>(null)
    val testDataPresent: StateFlow<Boolean?> = _testDataPresent

    private val _testDataBusy = MutableStateFlow(false)
    val testDataBusy: StateFlow<Boolean> = _testDataBusy

    private val _testDataMessage = MutableStateFlow<String?>(null)
    val testDataMessage: StateFlow<String?> = _testDataMessage

    val isDarkTheme: StateFlow<Boolean> = themeDataStore.isDarkTheme
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(
                5000
            ), false
        )

    val accentColor: StateFlow<AccentColor> = themeDataStore.accentColor
        .map { AccentColor.fromKey(it) }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(
                5000
            ), AccentColor.default
        )

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            userRepository.getProfile()
                .onSuccess { _uiState.value = ProfileUiState.Success(it) }
                .onFailure {
                    _uiState.value = ProfileUiState.Error(it.message ?: "Ошибка загрузки")
                }
        }
    }

    fun saveProfile(fio: String, phone: String, email: String) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            userRepository.updateProfile(fio, phone, email)
                .onSuccess {
                    _saveState.value = SaveState.Success

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

    fun saveStateSeen() {
        _saveState.value = SaveState.Idle
    }

    fun toggleTheme() {
        viewModelScope.launch { themeDataStore.setDarkTheme(!isDarkTheme.value) }
    }

    fun setAccentColor(accent: AccentColor) {
        viewModelScope.launch { themeDataStore.setAccentColor(accent.key) }
    }

    fun loadTestDataStatus() {
        viewModelScope.launch {
            adminRepository.getTestDataStatus()
                .onSuccess { _testDataPresent.value = it }
        }
    }

    fun toggleTestData() {
        if (_testDataBusy.value) return
        viewModelScope.launch {
            _testDataBusy.value = true
            adminRepository.toggleTestData()
                .onSuccess {
                    _testDataMessage.value = it
                    adminRepository.getTestDataStatus().onSuccess { p -> _testDataPresent.value = p }
                }
                .onFailure { _testDataMessage.value = it.message ?: "Не удалось изменить тестовые данные" }
            _testDataBusy.value = false
        }
    }

    fun testDataMessageShown() { _testDataMessage.value = null }

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
