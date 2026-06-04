package com.example.fitnessapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.InsideVisit
import com.example.fitnessapp.domain.model.ScanAction
import com.example.fitnessapp.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminVisitsUiState {
    object Loading : AdminVisitsUiState()
    data class Success(val inside: List<InsideVisit>) : AdminVisitsUiState()
    data class Error(val message: String) : AdminVisitsUiState()
}

@HiltViewModel
class AdminVisitsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminVisitsUiState>(AdminVisitsUiState.Loading)
    val uiState: StateFlow<AdminVisitsUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

    private val _pendingQuickExit = MutableStateFlow<QuickExitPrompt?>(null)
    val pendingQuickExit: StateFlow<QuickExitPrompt?> = _pendingQuickExit

    private val _pendingManualCheckout = MutableStateFlow<InsideVisit?>(null)
    val pendingManualCheckout: StateFlow<InsideVisit?> = _pendingManualCheckout

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AdminVisitsUiState.Loading
            fetch()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetch()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetch() {

        adminRepository.getInsideVisits()
            .onSuccess { _uiState.value = AdminVisitsUiState.Success(it) }
            .onFailure { _uiState.value = AdminVisitsUiState.Error(it.message ?: "Ошибка загрузки") }
    }

    fun onScanned(content: String?) {
        val token = content?.trim()
        if (token.isNullOrEmpty()) {
            _snackbar.value = "Некорректный QR-код"
            return
        }
        viewModelScope.launch {
            adminRepository.scanVisit(token, force = false)
                .onSuccess { result ->
                    when (result.action) {
                        ScanAction.ENTERED -> { _snackbar.value = "${result.fio} — вошёл"; fetch() }
                        ScanAction.EXITED -> { _snackbar.value = "${result.fio} — вышел"; fetch() }
                        ScanAction.WARN_QUICK_EXIT ->
                            _pendingQuickExit.value = QuickExitPrompt(token, result.fio)
                    }
                }
                .onFailure { _snackbar.value = it.message ?: "Не удалось отсканировать" }
        }
    }

    fun confirmQuickExit() {
        val prompt = _pendingQuickExit.value ?: return
        _pendingQuickExit.value = null
        viewModelScope.launch {
            adminRepository.scanVisit(prompt.token, force = true)
                .onSuccess { _snackbar.value = "${prompt.fio} — вышел"; fetch() }
                .onFailure { _snackbar.value = it.message ?: "Не удалось отсканировать" }
        }
    }

    fun dismissQuickExit() { _pendingQuickExit.value = null }

    fun requestManualCheckout(visit: InsideVisit) { _pendingManualCheckout.value = visit }
    fun dismissManualCheckout() { _pendingManualCheckout.value = null }

    fun confirmManualCheckout() {
        val visit = _pendingManualCheckout.value ?: return
        _pendingManualCheckout.value = null
        viewModelScope.launch {
            adminRepository.checkoutVisit(visit.userId)
                .onSuccess { _snackbar.value = "${visit.name} — выход отмечен вручную"; fetch() }
                .onFailure { _snackbar.value = it.message ?: "Не удалось отметить выход" }
        }
    }

    fun snackbarShown() { _snackbar.value = null }
}

data class QuickExitPrompt(val token: String, val fio: String)
