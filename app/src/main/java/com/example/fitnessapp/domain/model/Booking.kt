package com.example.fitnessapp.domain.model

data class Booking(
    val id: Long,
    val coachId: Long,
    val workoutId: Long,
    val slots: Int,
    val name: String,
    val extra: String?,
    val time: String,
    val clientIds: List<Int> = emptyList()
) {
    val availableSlots: Int get() = slots - clientIds.size
    val isFull: Boolean get() = availableSlots <= 0
}
