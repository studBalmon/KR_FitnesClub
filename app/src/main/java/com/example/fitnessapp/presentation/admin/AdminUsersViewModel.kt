package com.example.fitnessapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.AdminUser
import com.example.fitnessapp.domain.model.CoachType
import com.example.fitnessapp.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUsersUiState {
    object Loading : AdminUsersUiState()
    data class Success(val users: List<AdminUser>) : AdminUsersUiState()
    object Empty : AdminUsersUiState()
    data class Error(val message: String) : AdminUsersUiState()
}

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUsersUiState>(AdminUsersUiState.Loading)
    val uiState: StateFlow<AdminUsersUiState> = _uiState

    private val _coachTypes = MutableStateFlow<List<CoachType>>(emptyList())
    val coachTypes: StateFlow<List<CoachType>> = _coachTypes

    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private var allUsers: List<AdminUser> = emptyList()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AdminUsersUiState.Loading
            fetchUsers()
            adminRepository.getCoachTypes()
                .onSuccess { _coachTypes.value = it }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchUsers()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchUsers() {
        adminRepository.getUsers()
            .onSuccess { list ->
                allUsers = list
                applyFilter()
            }
            .onFailure {
                _uiState.value = AdminUsersUiState.Error(it.message ?: "Ошибка загрузки") }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val q = _searchQuery.value.trim().lowercase()
        val filtered = if (q.isEmpty()) allUsers
        else allUsers.filter {
            it.fio.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    it.phone.contains(q)
        }
        _uiState.value = if (filtered.isEmpty()) AdminUsersUiState.Empty
        else AdminUsersUiState.Success(filtered)
    }

    fun onLongPress(userId: Long) {
        _pendingDeleteId.value = userId
    }

    fun dismissDelete() {
        _pendingDeleteId.value = null
    }

    fun confirmDelete(userId: Long) {
        _pendingDeleteId.value = null
        viewModelScope.launch {
            adminRepository.deleteUser(userId)
                .onSuccess {
                    _snackbarMessage.value = "Пользователь удалён"
                    fetchUsers()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось удалить" }
        }
    }

    fun createUser(
        fio: String, phone: String, email: String, password: String,
        userTypeId: Int, coachTypeId: Int?
    ) {
        viewModelScope.launch {
            adminRepository.createUser(fio, phone, email, password, userTypeId, coachTypeId)
                .onSuccess {
                    _snackbarMessage.value = "Пользователь создан"
                    fetchUsers()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка создания" }
        }
    }

    fun updateUser(
        id: Long, fio: String, phone: String, email: String,
        newPassword: String?, coachTypeId: Int?
    ) {
        viewModelScope.launch {
            adminRepository.updateUser(id, fio, phone, email, newPassword, coachTypeId)
                .onSuccess {
                    _snackbarMessage.value = "Пользователь обновлён"
                    fetchUsers()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка обновления" }
        }
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }
}
