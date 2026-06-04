package com.example.fitnessapp.data.api.dto

import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("cardEndDate") val cardEndDate: String?,
    @SerializedName("userTypeId") val userTypeId: Int? = null
)

data class UpdateProfileRequest(
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String
)

data class PassTokenDto(
    @SerializedName("token") val token: String,
    @SerializedName("fio") val fio: String
)
