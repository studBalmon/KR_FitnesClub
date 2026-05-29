package com.example.fitnessapp.data.api.dto

import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("cardEndDate") val cardEndDate: String?
)

data class UpdateProfileRequest(
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String
)
