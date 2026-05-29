package com.example.fitnessapp.presentation.booking

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

sealed class BookingDetailState {
    object Loading : BookingDetailState()
    data class Success(val booking: Booking) : BookingDetailState()
    data class Error(val message: String) : BookingDetailState()
}

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: Long = checkNotNull(savedStateHandle["bookingId"])

    private val _state = MutableStateFlow<BookingDetailState>(BookingDetailState.Loading)
    val state: StateFlow<BookingDetailState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = BookingDetailState.Loading
            bookingRepository.getBookingById(bookingId)
                .onSuccess { _state.value = BookingDetailState.Success(it) }
                .onFailure { _state.value = BookingDetailState.Error(it.message ?: "Ошибка загрузки") }
        }
    }
}
