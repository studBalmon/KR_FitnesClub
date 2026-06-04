package com.example.fitnessapp.domain.repository

import com.example.fitnessapp.domain.model.AdminClientInfo
import com.example.fitnessapp.domain.model.AdminCoach
import com.example.fitnessapp.domain.model.AdminUser
import com.example.fitnessapp.domain.model.InsideVisit
import com.example.fitnessapp.domain.model.ScanResult
import com.example.fitnessapp.domain.model.CoachType
import com.example.fitnessapp.domain.model.WorkoutItem

interface AdminRepository {
    suspend fun getUsers(): Result<List<AdminUser>>
    suspend fun getCoachTypes(): Result<List<CoachType>>
    suspend fun getCoaches(): Result<List<AdminCoach>>
    suspend fun getClients(): Result<List<AdminClientInfo>>

    suspend fun getInsideVisits(): Result<List<InsideVisit>>
    suspend fun scanVisit(token: String, force: Boolean = false): Result<ScanResult>
    suspend fun checkoutVisit(userId: Long): Result<Unit>
    suspend fun createUser(
        fio: String, phone: String, email: String, password: String,
        userTypeId: Int, coachTypeId: Int?
    ): Result<Unit>

    suspend fun updateUser(
        id: Long, fio: String, phone: String, email: String,
        newPassword: String?, coachTypeId: Int?
    ): Result<Unit>

    suspend fun deleteUser(id: Long): Result<Unit>

    suspend fun extendSubscription(userId: Long, months: Int): Result<Unit>

    suspend fun getTestDataStatus(): Result<Boolean>

    suspend fun toggleTestData(): Result<String>

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

    suspend fun createCoachType(name: String): Result<Unit>
    suspend fun updateCoachType(id: Int, name: String): Result<Unit>
    suspend fun deleteCoachType(id: Int): Result<Unit>
}
