package com.example.fitnessapp.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CoachBookingsUiState {
    object Loading : CoachBookingsUiState()
    data class Success(val bookings: List<Booking>) : CoachBookingsUiState()
    object Empty : CoachBookingsUiState()
    data class Error(val message: String) : CoachBookingsUiState()
}

@HiltViewModel
class CoachBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoachBookingsUiState>(CoachBookingsUiState.Loading)
    val uiState: StateFlow<CoachBookingsUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId

    private val _deletingIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletingIds: StateFlow<Set<Long>> = _deletingIds

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CoachBookingsUiState.Loading
            fetchBookings()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchBookings()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchBookings() {
        bookingRepository.getCoachBookings()
            .onSuccess { list ->
                _uiState.value = if (list.isEmpty()) CoachBookingsUiState.Empty
                                 else CoachBookingsUiState.Success(list)
            }
            .onFailure { _uiState.value = CoachBookingsUiState.Error(it.message ?: "Ошибка загрузки") }
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
                    fetchBookings()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось удалить" }
            _deletingIds.value = _deletingIds.value - bookingId
        }
    }

    fun snackbarShown() { _snackbarMessage.value = null }
}
