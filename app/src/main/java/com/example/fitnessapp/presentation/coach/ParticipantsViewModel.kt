package com.example.fitnessapp.presentation.coach

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Participant
import com.example.fitnessapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ParticipantsUiState {
    object Loading : ParticipantsUiState()
    data class Success(val participants: List<Participant>) : ParticipantsUiState()
    object Empty : ParticipantsUiState()
    data class Error(val message: String) : ParticipantsUiState()
}

@HiltViewModel
class ParticipantsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: Long = savedStateHandle["bookingId"]!!

    private val _uiState = MutableStateFlow<ParticipantsUiState>(ParticipantsUiState.Loading)
    val uiState: StateFlow<ParticipantsUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ParticipantsUiState.Loading
            bookingRepository.getParticipants(bookingId)
                .onSuccess { list ->
                    _uiState.value = if (list.isEmpty()) ParticipantsUiState.Empty
                                     else ParticipantsUiState.Success(list)
                }
                .onFailure { _uiState.value = ParticipantsUiState.Error(it.message ?: "Ошибка загрузки") }
        }
    }
}
