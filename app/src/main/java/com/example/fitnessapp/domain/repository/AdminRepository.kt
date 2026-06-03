package com.example.fitnessapp.domain.repository

import com.example.fitnessapp.domain.model.AdminUser
import com.example.fitnessapp.domain.model.CoachType
import com.example.fitnessapp.domain.model.WorkoutItem

interface AdminRepository {
    suspend fun getUsers(): Result<List<AdminUser>>
    suspend fun getCoachTypes(): Result<List<CoachType>>
    suspend fun createUser(
        fio: String, phone: String, email: String, password: String,
        userTypeId: Int, coachTypeId: Int?
    ): Result<Unit>

    suspend fun updateUser(
        id: Long, fio: String, phone: String, email: String,
        newPassword: String?, coachTypeId: Int?
    ): Result<Unit>

    suspend fun deleteUser(id: Long): Result<Unit>

    // Типы занятий
    suspend fun getWorkouts(): Result<List<WorkoutItem>>
    suspend fun createWorkout(
        name: String,
        description: String?,
        duration: Int,
        coachTypeId: Int
    ): Result<Unit>

    suspend fun updateWorkout(
        id: Int,
        name: String,
        description: String?,
        duration: Int,
        coachTypeId: Int
    ): Result<Unit>

    suspend fun deleteWorkout(id: Int): Result<Unit>

    // Типы тренеров
    suspend fun createCoachType(name: String): Result<Unit>
    suspend fun updateCoachType(id: Int, name: String): Result<Unit>
    suspend fun deleteCoachType(id: Int): Result<Unit>
}
