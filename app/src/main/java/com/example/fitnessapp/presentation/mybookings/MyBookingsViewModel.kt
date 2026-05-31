package com.example.fitnessapp.presentation.mybookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import com.example.fitnessapp.domain.repository.UserRepository
import com.example.fitnessapp.domain.repository.WorkoutInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class MyBookingsUiState {
    object Loading : MyBookingsUiState()
    data class Success(val bookings: List<Booking>) : MyBookingsUiState()
    object Empty : MyBookingsUiState()
    data class Error(val message: String) : MyBookingsUiState()
}

enum class MyBookingSort(val labelRu: String) {
    DEFAULT("По умолчанию"),
    NAME_ASC("Название А → Я"),
    NAME_DESC("Название Я → А"),
    TIME_ASC("Время: раньше сначала"),
    TIME_DESC("Время: позже сначала")
}

data class MyFilterState(
    val sort:       MyBookingSort = MyBookingSort.DEFAULT,
    val workoutIds: Set<Int>      = emptySet()
)

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyBookingsUiState>(MyBookingsUiState.Loading)
    val uiState: StateFlow<MyBookingsUiState> = _uiState

    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId

    private val _deletingIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletingIds: StateFlow<Set<Long>> = _deletingIds

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _datesWithBookings = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val datesWithBookings: StateFlow<Map<LocalDate, Int>> = _datesWithBookings

    private val _filterState = MutableStateFlow(MyFilterState())
    val filterState: StateFlow<MyFilterState> = _filterState

    private val _workoutTypes = MutableStateFlow<List<WorkoutInfo>>(emptyList())
    val workoutTypes: StateFlow<List<WorkoutInfo>> = _workoutTypes

    private var allBookings: List<Booking> = emptyList()

    init {
        load()
        viewModelScope.launch { fetchWorkoutTypes() }
    }

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

    private suspend fun fetchWorkoutTypes() {
        userRepository.getWorkoutTypes().onSuccess { list -> _workoutTypes.value = list }
    }

    private suspend fun fetchMyBookings() {
        bookingRepository.getMyBookings()
            .onSuccess { list ->
                allBookings = list
                applyFilter()
            }
            .onFailure { _uiState.value = MyBookingsUiState.Error(it.message ?: "Ошибка загрузки") }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        applyFilter()
    }

    fun applyFilterState(fs: MyFilterState) {
        _filterState.value = fs
        applyFilter()
    }

    private fun applyFilter() {
        val fs = _filterState.value

        var withoutDate = allBookings
        if (fs.workoutIds.isNotEmpty()) withoutDate = withoutDate.filter {
            it.workoutId in fs.workoutIds.map { id -> id.toLong() }
        }
        _datesWithBookings.value = withoutDate.mapNotNull { it.date() }.groupingBy { it }.eachCount()

        var result = withoutDate.filter { it.date() == _selectedDate.value }
        result = when (fs.sort) {
            MyBookingSort.NAME_ASC  -> result.sortedBy { it.name }
            MyBookingSort.NAME_DESC -> result.sortedByDescending { it.name }
            MyBookingSort.TIME_ASC  -> result.sortedBy { it.time }
            MyBookingSort.TIME_DESC -> result.sortedByDescending { it.time }
            MyBookingSort.DEFAULT   -> result
        }

        _uiState.value = if (result.isEmpty()) MyBookingsUiState.Empty
                         else MyBookingsUiState.Success(result)
    }

    private fun Booking.date(): LocalDate? = runCatching {
        LocalDate.parse(time.take(10))
    }.getOrNull()

    fun onLongPress(bookingId: Long) { _pendingDeleteId.value = bookingId }
    fun dismissDelete() { _pendingDeleteId.value = null }

    fun confirmDelete(bookingId: Long) {
        _pendingDeleteId.value = null
        viewModelScope.launch {
            _deletingIds.value = _deletingIds.value + bookingId
            bookingRepository.leaveBooking(bookingId)
                .onSuccess {
                    _snackbarMessage.value = "Запись отменена"
                    fetchMyBookings()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось отменить запись" }
            _deletingIds.value = _deletingIds.value - bookingId
        }
    }

    fun snackbarShown() { _snackbarMessage.value = null }
}
