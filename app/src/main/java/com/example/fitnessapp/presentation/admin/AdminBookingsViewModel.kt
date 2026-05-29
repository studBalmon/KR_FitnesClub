package com.example.fitnessapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminBookingsUiState {
    object Loading : AdminBookingsUiState()
    data class Success(val bookings: List<Booking>) : AdminBookingsUiState()
    object Empty : AdminBookingsUiState()
    data class Error(val message: String) : AdminBookingsUiState()
}

@HiltViewModel
class AdminBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminBookingsUiState>(AdminBookingsUiState.Loading)
    val uiState: StateFlow<AdminBookingsUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId

    private val _deletingIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletingIds: StateFlow<Set<Long>> = _deletingIds

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    // Строка поиска по клиентским запросам
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var allBookings: List<Booking> = emptyList()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AdminBookingsUiState.Loading
            fetchAll()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchAll()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchAll() {
        bookingRepository.getAllBookings()
            .onSuccess { list ->
                allBookings = list
                applyFilter()
            }
            .onFailure { _uiState.value = AdminBookingsUiState.Error(it.message ?: "Ошибка загрузки") }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val q = _searchQuery.value.trim().lowercase()
        val filtered = if (q.isEmpty()) allBookings
                       else allBookings.filter { it.name.lowercase().contains(q) }
        _uiState.value = if (filtered.isEmpty()) AdminBookingsUiState.Empty
                         else AdminBookingsUiState.Success(filtered)
    }

    fun onLongPress(bookingId: Long) { _pendingDeleteId.value = bookingId }
    fun dismissDelete() { _pendingDeleteId.value = null }

    fun confirmDelete(bookingId: Long) {
        _pendingDeleteId.value = null
        viewModelScope.launch {
            _deletingIds.value = _deletingIds.value + bookingId
            bookingRepository.deleteBooking(bookingId)
                .onSuccess {
                    _snackbarMessage.value = "Занятие удалено"
                    fetchAll()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось удалить" }
            _deletingIds.value = _deletingIds.value - bookingId
        }
    }

    fun snackbarShown() { _snackbarMessage.value = null }
}
