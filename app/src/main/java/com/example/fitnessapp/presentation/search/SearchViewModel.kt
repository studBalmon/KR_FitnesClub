package com.example.fitnessapp.presentation.search

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

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<Booking>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    object Empty : SearchUiState()
}

private const val KEY_QUERY = "search_query"

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val query = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history

    init {
        loadHistory()
    }

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun search() {
        val q = query.value.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            searchHistoryRepository.addQuery(q)
            loadHistory()
            bookingRepository.searchBookings(q)
                .onSuccess { list ->
                    _uiState.value =
                        if (list.isEmpty()) SearchUiState.Empty
                        else SearchUiState.Success(list)
                }
                .onFailure {
                    _uiState.value = SearchUiState.Error(it.message ?: "Ошибка поиска")
                }
        }
    }

    fun clearQuery() {
        savedStateHandle[KEY_QUERY] = ""
        _uiState.value = SearchUiState.Idle
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

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = searchHistoryRepository.getHistory()
        }
    }
}
