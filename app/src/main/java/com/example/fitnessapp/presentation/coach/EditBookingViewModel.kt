package com.example.fitnessapp.presentation.coach

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditBookingLoadState {
    object Loading : EditBookingLoadState()
    data class Loaded(val booking: Booking) : EditBookingLoadState()
    data class Error(val message: String) : EditBookingLoadState()
}

sealed class EditBookingSaveState {
    object Idle : EditBookingSaveState()
    object Saving : EditBookingSaveState()
    object Success : EditBookingSaveState()
    data class Error(val message: String) : EditBookingSaveState()
}

@HiltViewModel
class EditBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: Long = savedStateHandle["bookingId"]!!

    private val _loadState = MutableStateFlow<EditBookingLoadState>(
        EditBookingLoadState.Loading
    )
    val loadState: StateFlow<EditBookingLoadState> = _loadState

    private val _saveState = MutableStateFlow<EditBookingSaveState>(
        EditBookingSaveState.Idle
    )
    val saveState: StateFlow<EditBookingSaveState> = _saveState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _loadState.value = EditBookingLoadState.Loading
            bookingRepository.getBookingById(bookingId)
                .onSuccess { _loadState.value = EditBookingLoadState.Loaded(it) }
                .onFailure {
                    _loadState.value = EditBookingLoadState.Error(
                        it.message ?: "Ошибка загрузки"
                    )
                }
        }
    }

    fun save(name: String, slots: Int, extra: String?, time: String) {
        viewModelScope.launch {
            _saveState.value = EditBookingSaveState.Saving
            bookingRepository.updateBooking(bookingId, name, slots, extra?.ifBlank { null }, time)
                .onSuccess { _saveState.value = EditBookingSaveState.Success }
                .onFailure {
                    _saveState.value = EditBookingSaveState.Error(
                        it.message ?: "Ошибка сохранения"
                    )
                }
        }
    }

    fun resetSaveState() {
        _saveState.value = EditBookingSaveState.Idle
    }
}
