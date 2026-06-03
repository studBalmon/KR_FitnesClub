package com.example.fitnessapp.data.api.dto

import com.google.gson.annotations.SerializedName

data class AdminUserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("userTypeId") val userTypeId: Int,
    @SerializedName("roleName") val roleName: String,
    @SerializedName("coachTypeId") val coachTypeId: Int?,
    @SerializedName("cardEndDate") val cardEndDate: String? = null
)

data class ExtendSubscriptionRequest(
    @SerializedName("months") val months: Int
)

data class AdminCoachDto(
    @SerializedName("id") val id: Long,          // coaches.id (== booking.coachId)
    @SerializedName("userId") val userId: Long,
    @SerializedName("fio") val fio: String,
    @SerializedName("coachTypeName") val coachTypeName: String? = null
)

data class AdminClientDto(
    @SerializedName("id") val id: Long,          // clients.id (== booking.clientIds)
    @SerializedName("userId") val userId: Long,
    @SerializedName("fio") val fio: String,
    @SerializedName("cardEndDate") val cardEndDate: String
)

data class TestDataStatusDto(
    @SerializedName("present") val present: Boolean
)

data class TestDataToggleDto(
    @SerializedName("action") val action: String,
    @SerializedName("message") val message: String
)

data class CoachTypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class AdminCreateUserRequest(
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("userTypeId") val userTypeId: Int,
    @SerializedName("coachTypeId") val coachTypeId: Int?
)

data class AdminUpdateUserRequest(
    @SerializedName("fio") val fio: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("newPassword") val newPassword: String?,
    @SerializedName("coachTypeId") val coachTypeId: Int?
)

data class WorkoutItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("coachTypeId") val coachTypeId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("duration") val duration: Int
)

data class WorkoutItemRequest(
    @SerializedName("coachTypeId") val coachTypeId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("duration") val duration: Int
)

data class CoachTypeRequest(
    @SerializedName("name") val name: String
)
