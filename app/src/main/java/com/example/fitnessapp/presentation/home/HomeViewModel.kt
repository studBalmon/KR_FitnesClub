package com.example.fitnessapp.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import com.example.fitnessapp.domain.repository.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class AllBookings(val bookings: List<Booking>) : HomeUiState()
    object SearchLoading : HomeUiState()
    data class SearchResults(val bookings: List<Booking>) : HomeUiState()
    object SearchEmpty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

private const val KEY_QUERY = "home_query"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history

    // Бронирования, на которые пользователь уже записан
    private val _myBookingIds = MutableStateFlow<Set<Long>>(emptySet())
    val myBookingIds: StateFlow<Set<Long>> = _myBookingIds

    // Бронирования, которые сейчас в процессе записи
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _joiningIds = MutableStateFlow<Set<Long>>(emptySet())
    val joiningIds: StateFlow<Set<Long>> = _joiningIds

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    init {
        loadAllBookings()
        loadHistory()
    }

    // ─── Загрузка всех занятий ────────────────────────────────────────────────

    fun loadAllBookings() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
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
        val allResult = bookingRepository.getAllBookings()
        val myResult = bookingRepository.getMyBookings()
        myResult.onSuccess { list -> _myBookingIds.value = list.map { it.id }.toSet() }
        allResult
            .onSuccess { _uiState.value = HomeUiState.AllBookings(it) }
            .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Ошибка загрузки") }
    }

    // ─── Поиск ───────────────────────────────────────────────────────────────

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
        if (value.isBlank()) {
            // Сброс к полному списку
            loadAllBookings()
        }
    }

    fun search() {
        val q = query.value.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _uiState.value = HomeUiState.SearchLoading
            searchHistoryRepository.addQuery(q)
            loadHistory()
            bookingRepository.searchBookings(q)
                .onSuccess { list ->
                    _uiState.value = if (list.isEmpty()) HomeUiState.SearchEmpty
                                     else HomeUiState.SearchResults(list)
                }
                .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Ошибка поиска") }
        }
    }

    fun clearQuery() {
        savedStateHandle[KEY_QUERY] = ""
        loadAllBookings()
    }

    fun selectHistoryItem(item: String) {
        savedStateHandle[KEY_QUERY] = item
        search()
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearHistory()
            _history.value = emptyList()
        }
    }

    // ─── Запись на занятие ────────────────────────────────────────────────────

    fun joinBooking(bookingId: Long) {
        if (_joiningIds.value.contains(bookingId)) return
        viewModelScope.launch {
            _joiningIds.value = _joiningIds.value + bookingId
            bookingRepository.joinBooking(bookingId)
                .onSuccess {
                    _snackbarMessage.value = "Вы успешно записаны!"
                    _myBookingIds.value = _myBookingIds.value + bookingId
                    refreshCurrent()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось записаться" }
            _joiningIds.value = _joiningIds.value - bookingId
        }
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }

    // ─── Вспомогательные ─────────────────────────────────────────────────────

    private fun refreshCurrent() {
        if (query.value.isBlank()) loadAllBookings() else search()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = searchHistoryRepository.getHistory()
        }
    }
}
