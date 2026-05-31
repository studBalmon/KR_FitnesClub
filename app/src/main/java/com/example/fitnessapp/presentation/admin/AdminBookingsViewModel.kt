package com.example.fitnessapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.AdminRepository
import com.example.fitnessapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class AdminBookingsUiState {
    object Loading : AdminBookingsUiState()
    data class Success(val bookings: List<Booking>) : AdminBookingsUiState()
    object Empty : AdminBookingsUiState()
    data class Error(val message: String) : AdminBookingsUiState()
}

enum class BookingSort(val labelRu: String) {
    DEFAULT("По умолчанию"),
    COACH_ASC("Тренер (по возр.)"),
    COACH_DESC("Тренер (по убыв.)"),
    NAME_ASC("Название А → Я"),
    NAME_DESC("Название Я → А")
}

data class CoachItem(val id: Long, val name: String, val specialization: String?)

data class WorkoutTypeItem(val id: Int, val name: String)

data class AdminFilterState(
    val sort:       BookingSort  = BookingSort.DEFAULT,
    val coachIds:   Set<Long>    = emptySet(),
    val workoutIds: Set<Int>     = emptySet(),
    val slotsFrom:  Int?         = null,
    val slotsTo:    Int?         = null
)

@HiltViewModel
class AdminBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminBookingsUiState>(AdminBookingsUiState.Loading)
    val uiState: StateFlow<AdminBookingsUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _pendingDeleteId = MutableStateFlow<Long?>(null)
    val pendingDeleteId: StateFlow<Long?> = _pendingDeleteId

    private val _deletingIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletingIds: StateFlow<Set<Long>> = _deletingIds

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _datesWithBookings = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val datesWithBookings: StateFlow<Map<LocalDate, Int>> = _datesWithBookings

    private val _filterState = MutableStateFlow(AdminFilterState())
    val filterState: StateFlow<AdminFilterState> = _filterState

    private val _coaches = MutableStateFlow<List<CoachItem>>(emptyList())
    val coaches: StateFlow<List<CoachItem>> = _coaches

    private val _workoutTypes = MutableStateFlow<List<WorkoutTypeItem>>(emptyList())
    val workoutTypes: StateFlow<List<WorkoutTypeItem>> = _workoutTypes

    private var allBookings: List<Booking> = emptyList()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AdminBookingsUiState.Loading
            fetchAll()
            fetchCoaches()
            fetchWorkoutTypes()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchAll()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchCoaches() {
        val coachTypeMap = mutableMapOf<Int, String>()
        adminRepository.getCoachTypes().onSuccess { types ->
            types.forEach { coachTypeMap[it.id] = it.name }
        }
        adminRepository.getUsers().onSuccess { users ->
            _coaches.value = users
                .filter { it.roleName == "COACH" }
                .map { CoachItem(it.id, it.fio, it.coachTypeId?.let { id -> coachTypeMap[id] }) }
                .sortedBy { it.name }
        }
    }

    private suspend fun fetchWorkoutTypes() {
        adminRepository.getWorkouts().onSuccess { workouts ->
            _workoutTypes.value = workouts
                .map { WorkoutTypeItem(it.id, it.name) }
                .sortedBy { it.name }
        }
    }

    private suspend fun fetchAll() {
        bookingRepository.getAllBookings()
            .onSuccess { list ->
                allBookings = list
                applyFilter()
            }
            .onFailure { _uiState.value = AdminBookingsUiState.Error(it.message ?: "Ошибка загрузки") }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        applyFilter()
    }

    fun applyFilterState(fs: AdminFilterState) {
        _filterState.value = fs
        applyFilter()
    }

    private fun applyFilter() {
        val q  = _searchQuery.value.trim().lowercase()
        val fs = _filterState.value

        // Применяем все фильтры кроме даты — для точек на календаре
        var withoutDate = allBookings
        if (q.isNotEmpty()) withoutDate = withoutDate.filter { it.name.lowercase().contains(q) }
        if (fs.coachIds.isNotEmpty())   withoutDate = withoutDate.filter { it.coachId   in fs.coachIds }
        if (fs.workoutIds.isNotEmpty()) withoutDate = withoutDate.filter { it.workoutId in fs.workoutIds.map { id -> id.toLong() } }
        fs.slotsFrom?.let { from -> withoutDate = withoutDate.filter { it.availableSlots >= from } }
        fs.slotsTo?.let   { to   -> withoutDate = withoutDate.filter { it.availableSlots <= to   } }
        _datesWithBookings.value = withoutDate.mapNotNull { it.date() }.groupingBy { it }.eachCount()

        // Фильтр по выбранной дате — для списка
        var result = withoutDate.filter { it.date() == _selectedDate.value }

        result = when (fs.sort) {
            BookingSort.COACH_ASC  -> result.sortedBy   { it.coachId }
            BookingSort.COACH_DESC -> result.sortedByDescending { it.coachId }
            BookingSort.NAME_ASC   -> result.sortedBy   { it.name }
            BookingSort.NAME_DESC  -> result.sortedByDescending { it.name }
            BookingSort.DEFAULT    -> result
        }

        _uiState.value = if (result.isEmpty()) AdminBookingsUiState.Empty
                         else AdminBookingsUiState.Success(result)
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
            bookingRepository.deleteBooking(bookingId)
                .onSuccess {
                    _snackbarMessage.value = "Занятие удалено"
                    fetchAll()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Не удалось удалить" }
            _deletingIds.value = _deletingIds.value - bookingId
        }
    }

    fun snackbarShown() { _snackbarMessage.value = null }
}
