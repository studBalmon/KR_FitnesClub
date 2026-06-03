package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.api.ApiService
import com.example.fitnessapp.data.api.dto.UpdateProfileRequest
import com.example.fitnessapp.domain.model.UserProfile
import com.example.fitnessapp.domain.repository.CoachInfo
import com.example.fitnessapp.domain.repository.UserRepository
import com.example.fitnessapp.domain.repository.WorkoutInfo
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: ApiService
) : UserRepository {

    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val dto = api.getProfile()
        UserProfile(
            dto.id,
            dto.fio,
            dto.phone,
            dto.email,
            dto.cardEndDate,
            dto.userTypeId ?: 3)
    }

    override suspend fun updateProfile(
        fio: String,
        phone: String,
        email: String): Result<Unit> =
        runCatching {
            api.updateProfile(UpdateProfileRequest(fio, phone, email))
        }.mapHttpError()

    override suspend fun getCoaches(): Result<List<CoachInfo>> = runCatching {
        api.getUsers()
            .filter { user -> user.userTypeId == 2 }
            .map { user -> CoachInfo(user.id, user.fio) }
            .sortedBy { coach -> coach.name }
    }

    override suspend fun getWorkoutTypes(): Result<List<WorkoutInfo>> = runCatching {
        api.getWorkouts()
            .map { w -> WorkoutInfo(w.id, w.name) }
            .sortedBy { w -> w.name }
    }
}

private fun <T> Result<T>.mapHttpError(): Result<T> = recoverCatching { e ->
    if (e is HttpException) {
        val body = e.response()?.errorBody()?.string()
        val message = try {
            Gson().fromJson(body, Map::class.java)["error"] as? String
        } catch (_: Exception) { null }
        throw Exception(message ?: "Ошибка сервера (${e.code()})")
    }
    throw e
}
