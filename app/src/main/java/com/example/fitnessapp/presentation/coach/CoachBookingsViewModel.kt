package com.example.fitnessapp.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import com.example.fitnessapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class CoachBookingsUiState {
    object Loading : CoachBookingsUiState()
    data class Success(val bookings: List<Booking>) : CoachBookingsUiState()
    object Empty : CoachBookingsUiState()
    data class Error(val message: String) : CoachBookingsUiState()
}

enum class CoachBookingSort(val labelRu: String) {
    DEFAULT("По умолчанию"),
    NAME_ASC("Название А → Я"),
    NAME_DESC("Название Я → А")
}

data class CoachWorkoutTypeItem(val id: Int, val name: String)

data class CoachFilterState(
    val sort: CoachBookingSort = CoachBookingSort.DEFAULT,
    val workoutIds: Set<Int> = emptySet(),
    val slotsFrom: Int? = null,
    val slotsTo: Int? = null
)

@HiltViewModel
class CoachBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoachBookingsUiState>(
        CoachBookingsUiState.Loading
    )
    val uiState: StateFlow<CoachBookingsUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId

    private val _deletingIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletingIds: StateFlow<Set<Long>> = _deletingIds

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _datesWithBookings = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val datesWithBookings: StateFlow<Map<LocalDate, Int>> = _datesWithBookings

    private val _filterState = MutableStateFlow(CoachFilterState())
    val filterState: StateFlow<CoachFilterState> = _filterState

    private val _workoutTypes = MutableStateFlow<List<CoachWorkoutTypeItem>>(emptyList())
    val workoutTypes: StateFlow<List<CoachWorkoutTypeItem>> = _workoutTypes

    private var allBookings: List<Booking> = emptyList()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CoachBookingsUiState.Loading
            fetchBookings()
            fetchWorkoutTypes()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchBookings()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchWorkoutTypes() {
        userRepository.getWorkoutTypes().onSuccess { list ->
            _workoutTypes.value = list.map { CoachWorkoutTypeItem(it.id, it.name) }
        }
    }

    private suspend fun fetchBookings() {
        bookingRepository.getCoachBookings()
            .onSuccess { list ->
                allBookings = list
                applyFilter()
            }
            .onFailure {
                _uiState.value = CoachBookingsUiState.Error(it.message ?: "Ошибка загрузки")
            }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        applyFilter()
    }

    fun applyFilterState(fs: CoachFilterState) {
        _filterState.value = fs
        applyFilter()
    }

    private fun applyFilter() {
        val fs = _filterState.value

        var withoutDate = allBookings
        if (fs.workoutIds.isNotEmpty()) withoutDate =
            withoutDate.filter { it.workoutId in fs.workoutIds.map { id -> id.toLong() } }
        fs.slotsFrom?.let { from -> withoutDate = withoutDate.filter { it.availableSlots >= from } }
        fs.slotsTo?.let { to -> withoutDate = withoutDate.filter { it.availableSlots <= to } }
        _datesWithBookings.value =
            withoutDate.mapNotNull { it.date() }.groupingBy { it }.eachCount()

        var result = withoutDate.filter { it.date() == _selectedDate.value }
        result = when (fs.sort) {
            CoachBookingSort.NAME_ASC -> result.sortedBy { it.name }
            CoachBookingSort.NAME_DESC -> result.sortedByDescending { it.name }
            CoachBookingSort.DEFAULT -> result
        }

        _uiState.value = if (result.isEmpty()) CoachBookingsUiState.Empty
        else CoachBookingsUiState.Success(result)
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
            bookingRepository.deleteBooking(bookingId)
                .onSuccess { _snackbarMessage.value = "Занятие удалено"; fetchBookings() }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось удалить" }
            _deletingIds.value = _deletingIds.value - bookingId
        }
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }

    private fun Booking.date(): LocalDate? = runCatching {
        LocalDate.parse(time.take(10))
    }.getOrNull()
}
