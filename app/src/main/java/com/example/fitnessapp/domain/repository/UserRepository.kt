package com.example.fitnessapp.domain.repository

import com.example.fitnessapp.domain.model.UserProfile

interface UserRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(fio: String, phone: String, email: String): Result<Unit>
}
