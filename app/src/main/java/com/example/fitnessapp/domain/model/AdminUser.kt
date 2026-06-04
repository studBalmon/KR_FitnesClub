package com.example.fitnessapp.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class AdminUser(
    val id: Long,
    val fio: String,
    val phone: String,
    val email: String,
    val userTypeId: Int,
    val roleName: String,
    val coachTypeId: Int?,
    val cardEndDate: LocalDate? = null
) {
    val roleLabel: String
        get() = when (roleName) {
            "ADMIN" -> "Администратор"
            "COACH" -> "Тренер"
            "CLIENT" -> "Клиент"
            else -> roleName
        }

    val daysLeft: Long?
        get() = cardEndDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }

    val isExpiringSoon: Boolean
        get() = daysLeft?.let { it in 0..6 } ?: false

    val isExpired: Boolean
        get() = daysLeft?.let { it < 0 } ?: false
}

data class CoachType(
    val id: Int,
    val name: String
)

data class AdminCoach(
    val id: Long,
    val userId: Long,
    val name: String,
    val coachTypeName: String?,
    val phone: String = ""
)

data class InsideVisit(
    val userId: Long,
    val name: String,
    val entryTime: String,
    val minutesInside: Long,
    val nextClassName: String? = null,
    val nextClassTime: String? = null
)

enum class ScanAction { ENTERED, EXITED, WARN_QUICK_EXIT }

data class ScanResult(
    val action: ScanAction,
    val fio: String
)

data class AdminClientInfo(
    val id: Long,
    val name: String,
    val cardEndDate: LocalDate?,
    val phone: String = ""
) {
    val daysLeft: Long?
        get() = cardEndDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }
    val isExpiringSoon: Boolean
        get() = daysLeft?.let { it in 0..6 } ?: false
    val isExpired: Boolean
        get() = daysLeft?.let { it < 0 } ?: false
    val isActive: Boolean
        get() = cardEndDate != null && !isExpired
}

data class WorkoutItem(
    val id: Int,
    val coachTypeId: Int,
    val name: String,
    val description: String?,
    val duration: Int
)
