package com.example.fitnessapp.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import com.example.fitnessapp.domain.repository.SearchHistoryRepository
import com.example.fitnessapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ClientBookingSort(val labelRu: String) {
    DEFAULT("По умолчанию"),
    COACH_ASC("Тренер (по возр.)"),
    COACH_DESC("Тренер (по убыв.)"),
    NAME_ASC("Название А → Я"),
    NAME_DESC("Название Я → А"),
    SLOTS_ASC("Мест: меньше сначала"),
    SLOTS_DESC("Мест: больше сначала")
}

data class ClientCoachItem(val id: Long, val name: String, val specialization: String?)
data class ClientWorkoutTypeItem(val id: Int, val name: String)

data class ClientFilterState(
    val sort: ClientBookingSort = ClientBookingSort.DEFAULT,
    val coachIds: Set<Long> = emptySet(),
    val workoutIds: Set<Int> = emptySet(),
    val slotsFrom: Int? = null,
    val slotsTo: Int? = null
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class AllBookings(val bookings: List<Booking>) : HomeUiState()
    object SearchLoading : HomeUiState()
    data class SearchResults(val bookings: List<Booking>) : HomeUiState()
    object SearchEmpty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

private const val KEY_QUERY = "home_query"
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history

    private val _myBookingIds = MutableStateFlow<Set<Long>>(emptySet())
    val myBookingIds: StateFlow<Set<Long>> = _myBookingIds

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _joiningIds = MutableStateFlow<Set<Long>>(emptySet())
    val joiningIds: StateFlow<Set<Long>> = _joiningIds

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())


    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _datesWithBookings = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val datesWithBookings: StateFlow<Map<LocalDate, Int>> = _datesWithBookings

    private val _filterState = MutableStateFlow(ClientFilterState())
    val filterState: StateFlow<ClientFilterState> = _filterState

    private val _workoutTypes = MutableStateFlow<List<ClientWorkoutTypeItem>>(emptyList())
    val workoutTypes: StateFlow<List<ClientWorkoutTypeItem>> = _workoutTypes

    private val _coaches = MutableStateFlow<List<ClientCoachItem>>(emptyList())
    val coaches: StateFlow<List<ClientCoachItem>> = _coaches

    private val _subscriptionActive = MutableStateFlow(true)
    val subscriptionActive: StateFlow<Boolean> = _subscriptionActive

    init {
        loadAllBookings()
        loadHistory()
        viewModelScope.launch {
            fetchWorkoutTypes()
            fetchCoaches()
            fetchSubscription()
        }
    }

    private suspend fun fetchSubscription() {
        userRepository.getProfile().onSuccess { profile ->
            val end = profile.cardEndDate?.let {
                runCatching { LocalDate.parse(it.take(10)) }.getOrNull()
            }
            _subscriptionActive.value = end == null || !end.isBefore(LocalDate.now())
        }
    }


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
            fetchSubscription()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchWorkoutTypes() {
        userRepository.getWorkoutTypes().onSuccess { list ->
            _workoutTypes.value = list.map { ClientWorkoutTypeItem(it.id, it.name) }
        }
    }

    private suspend fun fetchCoaches() {
        userRepository.getCoaches().onSuccess { list ->
            _coaches.value = list.map { ClientCoachItem(it.id, it.name, null) }
        }
    }

    private suspend fun fetchBookings() {
        val allResult = bookingRepository.getAllBookings()
        val myResult = bookingRepository.getMyBookings()
        myResult.onSuccess { list -> _myBookingIds.value = list.map { it.id }.toSet() }
        allResult
            .onSuccess { list ->
                _allBookings.value = list
                applyDateFilter()
            }
            .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Ошибка загрузки") }
    }


    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        if (query.value.isBlank()) applyDateFilter()
    }

    fun applyFilterState(fs: ClientFilterState) {
        _filterState.value = fs
        if (query.value.isBlank()) applyDateFilter()
    }

    private fun applyDateFilter() {
        val fs = _filterState.value
        var withoutDate = _allBookings.value
        if (fs.coachIds.isNotEmpty()) withoutDate = withoutDate.filter { it.coachId in fs.coachIds }
        if (fs.workoutIds.isNotEmpty()) withoutDate =
            withoutDate.filter { it.workoutId in fs.workoutIds.map { id -> id.toLong() } }
        fs.slotsFrom?.let { from -> withoutDate = withoutDate.filter { it.availableSlots >= from } }
        fs.slotsTo?.let { to -> withoutDate = withoutDate.filter { it.availableSlots <= to } }
        _datesWithBookings.value =
            withoutDate.mapNotNull { it.date() }.groupingBy { it }.eachCount()

        var result = withoutDate.filter { it.date() == _selectedDate.value }
        result = when (fs.sort) {
            ClientBookingSort.COACH_ASC -> result.sortedBy { it.coachId }
            ClientBookingSort.COACH_DESC -> result.sortedByDescending { it.coachId }
            ClientBookingSort.NAME_ASC -> result.sortedBy { it.name }
            ClientBookingSort.NAME_DESC -> result.sortedByDescending { it.name }
            ClientBookingSort.SLOTS_ASC -> result.sortedBy { it.availableSlots }
            ClientBookingSort.SLOTS_DESC -> result.sortedByDescending { it.availableSlots }
            ClientBookingSort.DEFAULT -> result
        }
        _uiState.value = HomeUiState.AllBookings(result)
    }


    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
        if (value.isBlank()) applyDateFilter()
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
        applyDateFilter()
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


    fun joinBooking(bookingId: Long) {
        if (!_subscriptionActive.value) {
            _snackbarMessage.value = "Абонемент неактивен"
            return
        }
        if (_joiningIds.value.contains(bookingId)) return
        viewModelScope.launch {
            _joiningIds.value = _joiningIds.value + bookingId
            bookingRepository.joinBooking(bookingId)
                .onSuccess {
                    _snackbarMessage.value = "Вы успешно записаны!"
                    _myBookingIds.value = _myBookingIds.value + bookingId
                    fetchBookings()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось записаться" }
            _joiningIds.value = _joiningIds.value - bookingId
        }
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }


    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = searchHistoryRepository.getHistory()
        }
    }

    private fun Booking.date(): LocalDate? = runCatching {
        LocalDate.parse(time.take(10))
    }.getOrNull()
}
