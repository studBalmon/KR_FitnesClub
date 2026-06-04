package com.example.fitnessapp.domain.repository

import com.example.fitnessapp.domain.model.UserProfile

data class CoachInfo(val id: Long, val name: String)
data class WorkoutInfo(val id: Int, val name: String)
data class PassToken(val token: String, val fio: String)

interface UserRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(fio: String, phone: String, email: String): Result<Unit>
    suspend fun getCoaches(): Result<List<CoachInfo>>
    suspend fun getWorkoutTypes(): Result<List<WorkoutInfo>>
    suspend fun getPassToken(): Result<PassToken>
}
