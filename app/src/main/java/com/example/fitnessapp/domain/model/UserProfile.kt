package com.example.fitnessapp.domain.model

data class UserProfile(
    val id: Long,
    val fio: String,
    val phone: String,
    val email: String,
    val cardEndDate: String?,
    val userTypeId: Int = 3  // 2=COACH, 3=CLIENT
) {
    val isCoach: Boolean get() = userTypeId == 2
}
