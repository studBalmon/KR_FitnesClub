package com.example.fitnessapp.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreateBookingState {
    object Idle : CreateBookingState()
    object Loading : CreateBookingState()
    object Success : CreateBookingState()
    data class Error(val message: String) : CreateBookingState()
}

@HiltViewModel
class CreateBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CreateBookingState>(CreateBookingState.Idle)
    val state: StateFlow<CreateBookingState> = _state

    fun create(name: String, slots: Int, extra: String?, time: String) {
        viewModelScope.launch {
            _state.value = CreateBookingState.Loading
            bookingRepository.createBooking(name, slots, extra?.ifBlank { null }, time)
                .onSuccess { _state.value = CreateBookingState.Success }
                .onFailure { _state.value = CreateBookingState.Error(it.message ?: "Ошибка создания") }
        }
    }

    fun resetState() { _state.value = CreateBookingState.Idle }
}
