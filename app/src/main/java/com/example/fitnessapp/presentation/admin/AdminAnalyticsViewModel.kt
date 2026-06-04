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
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

// ── Общие ────────────────────────────────────────────────────────────────────
data class WorkoutStat(val name: String, val bookings: Int, val enrolled: Int)
data class DayLoad(val label: String, val count: Int)

// ── Главный экран (ресепшен / ежедневно) ──────────────────────────────────────
data class TodayClass(
    val time: String,
    val name: String,
    val coach: String,
    val enrolled: Int,
    val slots: Int
)

data class ClientExpiring(val name: String, val daysLeft: Long)

/** Тренер, чьё ближайшее занятие скоро, но он ещё не в зале. */
data class CoachLate(val name: String, val minutesUntil: Long, val className: String)

data class DashboardData(
    val classesToday: Int = 0,
    val enrolledToday: Int = 0,
    val freeSlotsToday: Int = 0,
    val todaySchedule: List<TodayClass> = emptyList(),
    val expiringSoon: Int = 0,
    val expired: Int = 0,
    val expiringList: List<ClientExpiring> = emptyList(),
    val totalClients: Int = 0,
    val activeSubscriptions: Int = 0,
    val totalCoaches: Int = 0,
    val upcomingClasses: Int = 0,
    val loadByDay: List<DayLoad> = emptyList(),
    // Тренеры, у которых сегодня есть занятия и которые сейчас внутри клуба
    val coachesTodayInside: Int = 0,
    val coachesTodayTotal: Int = 0,
    // Тренеры, чьё занятие начинается < 15 мин, но их ещё нет в зале
    val coachesLate: List<CoachLate> = emptyList()
)

// ── Аналитика по тренерам ──────────────────────────────────────────────────────
data class CoachStat(
    val name: String,
    val coachType: String?,
    val bookings: Int,
    val enrolled: Int,
    val slots: Int,
    val fillRate: Int,        // 0-100%
    val hours: Int,           // проведено человеко-часов
    val avgGroup: Double,     // среднее участников на занятие
    val uniqueClients: Int
)

data class CoachesData(
    val coachCount: Int = 0,
    val avgFillRate: Int = 0,
    val totalHours: Int = 0,
    val totalClasses: Int = 0,
    val coaches: List<CoachStat> = emptyList()
)

// ── Аналитика по клиентам ───────────────────────────────────────────────────────
data class ClientRank(val name: String, val visits: Int, val hours: Double)
data class FreqBucket(val label: String, val count: Int)

data class ClientsData(
    val totalClients: Int = 0,
    val active: Int = 0,
    val expiringSoon: Int = 0,
    val expired: Int = 0,
    val withBookings: Int = 0,
    val withoutBookings: Int = 0,
    val avgBookingsPerClient: Double = 0.0,
    val avgHoursPerActive: Double = 0.0,
    val totalHours: Int = 0,
    val freq: List<FreqBucket> = emptyList(),
    val topByVisits: List<ClientRank> = emptyList(),
    val topByHours: List<ClientRank> = emptyList(),
    val topWorkouts: List<WorkoutStat> = emptyList(),
    val expiringList: List<ClientExpiring> = emptyList()
)

data class AnalyticsData(
    val dashboard: DashboardData = DashboardData(),
    val coaches: CoachesData = CoachesData(),
    val clients: ClientsData = ClientsData()
)

sealed class AdminAnalyticsUiState {
    object Loading : AdminAnalyticsUiState()
    data class Success(val data: AnalyticsData) : AdminAnalyticsUiState()
    data class Error(val message: String) : AdminAnalyticsUiState()
}

@HiltViewModel
class AdminAnalyticsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminAnalyticsUiState>(AdminAnalyticsUiState.Loading)
    val uiState: StateFlow<AdminAnalyticsUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AdminAnalyticsUiState.Loading
            compute()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            compute()
            _isRefreshing.value = false
        }
    }

    private suspend fun compute() {
        val bookings = bookingRepository.getAllBookings().getOrElse {
            _uiState.value = AdminAnalyticsUiState.Error(it.message ?: "Ошибка загрузки")
            return
        }
        val workouts = adminRepository.getWorkouts().getOrDefault(emptyList())
        val coaches  = adminRepository.getCoaches().getOrDefault(emptyList())
        val clients  = adminRepository.getClients().getOrDefault(emptyList())
        val insideUserIds = adminRepository.getInsideVisits().getOrDefault(emptyList())
            .map { it.userId }.toSet()

        // карты-справочники
        val workoutName     = workouts.associate { it.id.toLong() to it.name }
        val workoutDuration = workouts.associate { it.id.toLong() to it.duration }   // минуты
        val coachName       = coaches.associate { it.id to it.name }
        val coachType       = coaches.associate { it.id to it.coachTypeName }
        val coachUserId     = coaches.associate { it.id to it.userId }
        val clientName      = clients.associate { it.id to it.name }

        val today = LocalDate.now()

        // ── Дашборд ─────────────────────────────────────────────────────────
        val todayBookings = bookings.filter { it.date() == today }.sortedBy { it.time }
        val enrolledToday = todayBookings.sumOf { it.clientIds.size }
        val slotsToday    = todayBookings.sumOf { it.slots }
        val todaySchedule = todayBookings.map {
            TodayClass(
                time = it.time.substringAfter("T").take(5),
                name = it.name,
                coach = coachName[it.coachId] ?: "—",
                enrolled = it.clientIds.size,
                slots = it.slots
            )
        }
        val expiringClients = clients.filter { it.isExpiringSoon }.sortedBy { it.daysLeft }
        // тренеры с занятиями сегодня и сколько из них сейчас внутри
        val todayCoachIds = todayBookings.map { it.coachId }.toSet()
        val coachesTodayInside = todayCoachIds.count { cid ->
            coachUserId[cid]?.let { it in insideUserIds } ?: false
        }

        // тренеры, чьё ближайшее занятие начинается < 15 минут, но их ещё нет в зале
        val nowDt = LocalDateTime.now()
        val coachesLate = coaches.mapNotNull { c ->
            val next = bookings
                .filter { it.coachId == c.id }
                .mapNotNull { b -> b.dateTime()?.let { dt -> b to dt } }
                .filter { (_, dt) -> dt.toLocalDate() == today && !dt.isBefore(nowDt) }
                .minByOrNull { (_, dt) -> dt }
                ?: return@mapNotNull null
            val mins = Duration.between(nowDt, next.second).toMinutes()
            val inside = coachUserId[c.id]?.let { it in insideUserIds } ?: false
            if (mins in 0..14 && !inside) CoachLate(c.name, mins, next.first.name) else null
        }.sortedBy { it.minutesUntil }
        val dashboard = DashboardData(
            classesToday = todayBookings.size,
            enrolledToday = enrolledToday,
            freeSlotsToday = (slotsToday - enrolledToday).coerceAtLeast(0),
            todaySchedule = todaySchedule,
            expiringSoon = expiringClients.size,
            expired = clients.count { it.isExpired },
            expiringList = expiringClients.take(5).map { ClientExpiring(it.name, it.daysLeft ?: 0) },
            totalClients = clients.size,
            activeSubscriptions = clients.count { it.isActive },
            totalCoaches = coaches.size,
            upcomingClasses = bookings.count { b -> b.date()?.let { !it.isBefore(today) } ?: false },
            loadByDay = enrollmentsByDay(bookings),
            coachesTodayInside = coachesTodayInside,
            coachesTodayTotal = todayCoachIds.size,
            coachesLate = coachesLate
        )

        // ── Тренеры ─────────────────────────────────────────────────────────
        val byCoach = bookings.groupBy { it.coachId }
        val coachStats = coaches.map { c ->
            val list = byCoach[c.id] ?: emptyList()
            val enrolled = list.sumOf { it.clientIds.size }
            val slots = list.sumOf { it.slots }
            val minutes = list.sumOf { b -> (workoutDuration[b.workoutId] ?: 0).toLong() * b.clientIds.size }
            CoachStat(
                name = c.name,
                coachType = c.coachTypeName,
                bookings = list.size,
                enrolled = enrolled,
                slots = slots,
                fillRate = if (slots > 0) enrolled * 100 / slots else 0,
                hours = (minutes / 60).toInt(),
                avgGroup = if (list.isNotEmpty()) enrolled.toDouble() / list.size else 0.0,
                uniqueClients = list.flatMap { it.clientIds }.distinct().size
            )
        }.sortedWith(compareByDescending<CoachStat> { it.enrolled }.thenByDescending { it.bookings })

        val allSlots = bookings.sumOf { it.slots }
        val allEnrolled = bookings.sumOf { it.clientIds.size }
        val coachesData = CoachesData(
            coachCount = coaches.size,
            avgFillRate = if (allSlots > 0) allEnrolled * 100 / allSlots else 0,
            totalHours = coachStats.sumOf { it.hours },
            totalClasses = bookings.size,
            coaches = coachStats
        )

        // ── Клиенты ─────────────────────────────────────────────────────────
        val visitsByClient = bookings.flatMap { it.clientIds }.groupingBy { it }.eachCount()
        val minutesByClient = HashMap<Int, Long>()
        bookings.forEach { b ->
            val d = (workoutDuration[b.workoutId] ?: 0).toLong()
            b.clientIds.forEach { cid -> minutesByClient[cid] = (minutesByClient[cid] ?: 0) + d }
        }
        val withBookings = visitsByClient.size
        val totalClients = clients.size
        val totalEnrollments = visitsByClient.values.sum()
        val totalClientMinutes = minutesByClient.values.sum()

        val freq = listOf(
            FreqBucket("Без записей", (totalClients - withBookings).coerceAtLeast(0)),
            FreqBucket("1–2", visitsByClient.values.count { it in 1..2 }),
            FreqBucket("3–5", visitsByClient.values.count { it in 3..5 }),
            FreqBucket("6+",  visitsByClient.values.count { it >= 6 })
        )

        val topByVisits = visitsByClient.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { (cid, v) ->
                ClientRank(
                    name = clientName[cid.toLong()] ?: "Клиент #$cid",
                    visits = v,
                    hours = (minutesByClient[cid] ?: 0) / 60.0
                )
            }
        val topByHours = minutesByClient.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { (cid, min) ->
                ClientRank(
                    name = clientName[cid.toLong()] ?: "Клиент #$cid",
                    visits = visitsByClient[cid] ?: 0,
                    hours = min / 60.0
                )
            }

        val topWorkouts = bookings
            .groupBy { it.workoutId }
            .map { (wid, list) ->
                WorkoutStat(
                    name = workoutName[wid] ?: "Занятие #$wid",
                    bookings = list.size,
                    enrolled = list.sumOf { b -> b.clientIds.size }
                )
            }
            .sortedWith(compareByDescending<WorkoutStat> { it.enrolled }.thenByDescending { it.bookings })
            .take(6)

        val clientsData = ClientsData(
            totalClients = totalClients,
            active = clients.count { it.isActive },
            expiringSoon = clients.count { it.isExpiringSoon },
            expired = clients.count { it.isExpired },
            withBookings = withBookings,
            withoutBookings = (totalClients - withBookings).coerceAtLeast(0),
            avgBookingsPerClient = if (totalClients > 0) totalEnrollments.toDouble() / totalClients else 0.0,
            avgHoursPerActive = if (withBookings > 0) totalClientMinutes / 60.0 / withBookings else 0.0,
            totalHours = (totalClientMinutes / 60).toInt(),
            freq = freq,
            topByVisits = topByVisits,
            topByHours = topByHours,
            topWorkouts = topWorkouts,
            expiringList = clients.filter { it.isExpiringSoon }
                .sortedBy { it.daysLeft }
                .map { ClientExpiring(it.name, it.daysLeft ?: 0) }
        )

        _uiState.value = AdminAnalyticsUiState.Success(
            AnalyticsData(dashboard = dashboard, coaches = coachesData, clients = clientsData)
        )
    }

    private fun enrollmentsByDay(bookings: List<Booking>): List<DayLoad> {
        val dayNames = mapOf(
            DayOfWeek.MONDAY to "Пн", DayOfWeek.TUESDAY to "Вт", DayOfWeek.WEDNESDAY to "Ср",
            DayOfWeek.THURSDAY to "Чт", DayOfWeek.FRIDAY to "Пт", DayOfWeek.SATURDAY to "Сб",
            DayOfWeek.SUNDAY to "Вс"
        )
        val byDay = bookings
            .mapNotNull { b -> b.date()?.dayOfWeek?.let { it to b.clientIds.size } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, v) -> v.sum() }
        return DayOfWeek.entries.map { DayLoad(dayNames[it] ?: "", byDay[it] ?: 0) }
    }

    private fun Booking.date(): LocalDate? = runCatching {
        LocalDate.parse(time.take(10))
    }.getOrNull()

    private fun Booking.dateTime(): LocalDateTime? = runCatching {
        LocalDateTime.parse(time.take(19))
    }.getOrNull()
}
