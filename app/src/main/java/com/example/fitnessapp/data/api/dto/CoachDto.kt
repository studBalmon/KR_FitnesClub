package com.example.fitnessapp.data.api.dto

import com.google.gson.annotations.SerializedName

data class CreateBookingRequest(
    @SerializedName("name") val name: String,
    @SerializedName("slots") val slots: Int,
    @SerializedName("extra") val extra: String?,
    @SerializedName("time") val time: String   
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

data class CoachListDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fio") val fio: String,
    @SerializedName("coachTypeName") val coachTypeName: String? = null
)
