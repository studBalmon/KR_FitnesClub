package com.example.fitnessapp.data.api.dto

import com.example.fitnessapp.domain.model.Booking
import com.google.gson.annotations.SerializedName

data class BookingDto(
    @SerializedName("id") val id: Long,
    @SerializedName("coachId") val coachId: Long,
    @SerializedName("workoutId") val workoutId: Long,
    @SerializedName("slots") val slots: Int,
    @SerializedName("name") val name: String,
    @SerializedName("extra") val extra: String?,
    @SerializedName("time") val time: String,
    // Сервер возвращает List<Int> (clientId-шники), поле может отсутствовать
    @SerializedName("clients") val clients: List<Int>? = null
)

fun BookingDto.toDomain() = Booking(
    id = id,
    coachId = coachId,
    workoutId = workoutId,
    slots = slots,
    name = name,
    extra = extra,
    time = time,
    clientIds = clients ?: emptyList()
)
