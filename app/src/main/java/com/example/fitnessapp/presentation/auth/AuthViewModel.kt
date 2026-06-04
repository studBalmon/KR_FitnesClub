package com.example.fitnessapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.api.ServerUrlProvider
import com.example.fitnessapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val serverUrlProvider: ServerUrlProvider
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private val _serverUrl = MutableStateFlow(serverUrlProvider.baseUrl)
    val serverUrl: StateFlow<String> = _serverUrl

    private val _serverSavedMessage = MutableStateFlow<String?>(null)
    val serverSavedMessage: StateFlow<String?> = _serverSavedMessage

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            serverUrlProvider.setBaseUrl(url)
            _serverUrl.value = serverUrlProvider.baseUrl
            _serverSavedMessage.value = "Адрес сервера сохранён: ${serverUrlProvider.baseUrl}"
        }
    }

    fun serverSavedMessageShown() { _serverSavedMessage.value = null }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess { _state.value = AuthState.Success }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Ошибка входа") }
        }
    }

    fun register(fio: String, phone: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            authRepository.register(fio, phone, email, password)
                .onSuccess { _state.value = AuthState.Success }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Ошибка регистрации") }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}
