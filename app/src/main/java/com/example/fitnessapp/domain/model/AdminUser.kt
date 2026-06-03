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

    /** Сколько дней осталось до конца абонемента (отрицательное = истёк). null если не клиент. */
    val daysLeft: Long?
        get() = cardEndDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }

    /** Абонемент действует, но истекает менее чем через 7 дней. */
    val isExpiringSoon: Boolean
        get() = daysLeft?.let { it in 0..6 } ?: false

    /** Абонемент уже истёк. */
    val isExpired: Boolean
        get() = daysLeft?.let { it < 0 } ?: false
}

data class CoachType(
    val id: Int,
    val name: String
)

/** Тренер для аналитики: id = coaches.id (совпадает с booking.coachId). */
data class AdminCoach(
    val id: Long,
    val name: String,
    val coachTypeName: String?
)

/** Клиент для аналитики: id = clients.id (совпадает с booking.clientIds). */
data class AdminClientInfo(
    val id: Long,
    val name: String,
    val cardEndDate: LocalDate?
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
