package com.example.fitnessapp.domain.model

data class AdminUser(
    val id: Long,
    val fio: String,
    val phone: String,
    val email: String,
    val userTypeId: Int,
    val roleName: String,
    val coachTypeId: Int?
) {
    val roleLabel: String
        get() = when (roleName) {
            "ADMIN" -> "Администратор"
            "COACH" -> "Тренер"
            "CLIENT" -> "Клиент"
            else -> roleName
        }
}

data class CoachType(
    val id: Int,
    val name: String
)

data class WorkoutItem(
    val id: Int,
    val coachTypeId: Int,
    val name: String,
    val description: String?,
    val duration: Int
)
