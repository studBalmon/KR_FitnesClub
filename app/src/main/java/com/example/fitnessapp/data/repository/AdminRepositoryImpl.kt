package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.api.ApiService
import com.example.fitnessapp.data.api.dto.AdminCreateUserRequest
import com.example.fitnessapp.data.api.dto.AdminUpdateUserRequest
import com.example.fitnessapp.data.api.dto.CoachTypeRequest
import com.example.fitnessapp.data.api.dto.WorkoutItemRequest
import com.example.fitnessapp.domain.model.AdminUser
import com.example.fitnessapp.domain.model.CoachType
import com.example.fitnessapp.domain.model.WorkoutItem
import com.example.fitnessapp.domain.repository.AdminRepository
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val api: ApiService
) : AdminRepository {

    override suspend fun getUsers(): Result<List<AdminUser>> = runCatching {
        api.getAdminUsers().map {
            AdminUser(
                it.id,
                it.fio,
                it.phone,
                it.email,
                it.userTypeId,
                it.roleName,
                it.coachTypeId)
        }
    }

    override suspend fun getCoachTypes(): Result<List<CoachType>> = runCatching {
        api.getCoachTypes().map { CoachType(it.id, it.name) }
    }

    override suspend fun createUser(
        fio: String,
        phone: String,
        email: String,
        password: String,
        userTypeId: Int,
        coachTypeId: Int?
    ): Result<Unit> = runCatching {
        api.createAdminUser(
            AdminCreateUserRequest(
                fio,
                phone,
                email,
                password,
                userTypeId,
                coachTypeId
            )
        )
    }.mapHttpError()

    override suspend fun updateUser(
        id: Long,
        fio: String,
        phone: String,
        email: String,
        newPassword: String?,
        coachTypeId: Int?
    ): Result<Unit> = runCatching {
        api.updateAdminUser(id,
            AdminUpdateUserRequest(
                fio,
                phone,
                email,
                newPassword,
                coachTypeId))
    }.mapHttpError()

    override suspend fun deleteUser(id: Long): Result<Unit> = runCatching {
        api.deleteAdminUser(id)
    }.mapHttpError()

    override suspend fun getWorkouts(): Result<List<WorkoutItem>> = runCatching {
        api.getAdminWorkouts()
            .map { WorkoutItem(
                it.id,
                it.coachTypeId,
                it.name,
                it.description,
                it.duration) }
    }

    override suspend fun createWorkout(
        name: String,
        description: String?,
        duration: Int,
        coachTypeId: Int
    ): Result<Unit> = runCatching {
        api.createAdminWorkout(
            WorkoutItemRequest(
                coachTypeId,
                name,
                description,
                duration))
    }.mapHttpError()

    override suspend fun updateWorkout(
        id: Int,
        name: String,
        description: String?,
        duration: Int,
        coachTypeId: Int
    ): Result<Unit> = runCatching {
        api.updateAdminWorkout(
            id,
            WorkoutItemRequest(
                coachTypeId,
                name,
                description,
                duration))
    }.mapHttpError()

    override suspend fun deleteWorkout(id: Int): Result<Unit> = runCatching {
        api.deleteAdminWorkout(id)
    }.mapHttpError()

    override suspend fun createCoachType(name: String): Result<Unit> = runCatching {
        api.createCoachType(CoachTypeRequest(name))
    }.mapHttpError()

    override suspend fun updateCoachType(id: Int, name: String): Result<Unit> = runCatching {
        api.updateCoachType(id, CoachTypeRequest(name))
    }.mapHttpError()

    override suspend fun deleteCoachType(id: Int): Result<Unit> = runCatching {
        api.deleteCoachType(id)
    }.mapHttpError()
}

private fun <T> Result<T>.mapHttpError(): Result<T> = recoverCatching { e ->
    if (e is HttpException) {
        val body = e.response()?.errorBody()?.string()
        val message = try {
            Gson().fromJson(body, Map::class.java)["error"] as? String
        } catch (_: Exception) {
            null
        }
        throw Exception(message ?: "Ошибка сервера (${e.code()})")
    }
    throw e
}
