package com.example.fitnessapp.presentation.mybookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MyBookingsUiState {
    object Loading : MyBookingsUiState()
    data class Success(val bookings: List<Booking>) : MyBookingsUiState()
    object Empty : MyBookingsUiState()
    data class Error(val message: String) : MyBookingsUiState()
}

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyBookingsUiState>(MyBookingsUiState.Loading)
    val uiState: StateFlow<MyBookingsUiState> = _uiState

    // ID карточки выбранной долгим нажатием (ожидает подтверждения удаления)
    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId

    // ID записей в процессе удаления
    private val _deletingIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletingIds: StateFlow<Set<Long>> = _deletingIds

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MyBookingsUiState.Loading
            fetchMyBookings()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchMyBookings()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchMyBookings() {
        bookingRepository.getMyBookings()
            .onSuccess { list ->
                _uiState.value = if (list.isEmpty()) MyBookingsUiState.Empty
                                 else MyBookingsUiState.Success(list)
            }
            .onFailure { _uiState.value = MyBookingsUiState.Error(it.message ?: "Ошибка загрузки") }
    }

    fun onLongPress(bookingId: Long) {
        _pendingDeleteId.value = bookingId
    }

    fun dismissDelete() {
        _pendingDeleteId.value = null
    }

    fun confirmDelete(bookingId: Long) {
        _pendingDeleteId.value = null
        viewModelScope.launch {
            _deletingIds.value = _deletingIds.value + bookingId
            bookingRepository.leaveBooking(bookingId)
                .onSuccess {
                    _snackbarMessage.value = "Запись отменена"
                    fetchMyBookings()
                }
                .onFailure {
                    _snackbarMessage.value = it.message ?: "Не удалось отменить запись"
                }
            _deletingIds.value = _deletingIds.value - bookingId
        }
    }

    fun snackbarShown() { _snackbarMessage.value = null }
}
