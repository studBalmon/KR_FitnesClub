package com.example.fitnessapp.data.api.dto

import com.google.gson.annotations.SerializedName

data class CreateBookingRequest(
    @SerializedName("name") val name: String,
    @SerializedName("slots") val slots: Int,
    @SerializedName("extra") val extra: String?,
    @SerializedName("time") val time: String   // "2024-01-15T10:30:00"
)

data class UpdateBookingRequest(
    @SerializedName("name") val name: String,
    @SerializedName("slots") val slots: Int,
    @SerializedName("extra") val extra: String?,
    @SerializedName("time") val time: String
)

data class ParticipantDto(
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String
)

data class CreateBookingResponse(
    @SerializedName("id") val id: Long
)
