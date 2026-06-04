package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.api.ApiService
import com.example.fitnessapp.data.api.dto.AdminCreateUserRequest
import com.example.fitnessapp.data.api.dto.AdminUpdateUserRequest
import com.example.fitnessapp.data.api.dto.ExtendSubscriptionRequest
import com.example.fitnessapp.data.api.dto.ScanRequestDto
import com.example.fitnessapp.data.api.dto.CoachTypeRequest
import com.example.fitnessapp.data.api.dto.WorkoutItemRequest
import com.example.fitnessapp.domain.model.AdminClientInfo
import com.example.fitnessapp.domain.model.AdminCoach
import com.example.fitnessapp.domain.model.AdminUser
import com.example.fitnessapp.domain.model.InsideVisit
import com.example.fitnessapp.domain.model.ScanAction
import com.example.fitnessapp.domain.model.ScanResult
import com.example.fitnessapp.domain.model.CoachType
import com.example.fitnessapp.domain.model.WorkoutItem
import com.example.fitnessapp.domain.repository.AdminRepository
import com.google.gson.Gson
import retrofit2.HttpException
import java.time.LocalDate
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
                it.coachTypeId,
                cardEndDate = it.cardEndDate?.let { d -> runCatching { LocalDate.parse(d) }.getOrNull() }
            )
        }
    }

    override suspend fun extendSubscription(userId: Long, months: Int): Result<Unit> = runCatching {
        api.extendSubscription(userId, ExtendSubscriptionRequest(months))
    }.mapHttpError()

    override suspend fun getTestDataStatus(): Result<Boolean> = runCatching {
        api.getTestDataStatus().present
    }.mapHttpError()

    override suspend fun toggleTestData(): Result<String> = runCatching {
        api.toggleTestData().message
    }.mapHttpError()

    override suspend fun getCoachTypes(): Result<List<CoachType>> = runCatching {
        api.getCoachTypes().map { CoachType(it.id, it.name) }
    }

    override suspend fun getCoaches(): Result<List<AdminCoach>> = runCatching {
        api.getAdminCoaches().map { AdminCoach(it.id, it.userId, it.fio, it.coachTypeName) }
    }

    override suspend fun getInsideVisits(): Result<List<InsideVisit>> = runCatching {
        api.getInsideVisits().map {
            InsideVisit(it.userId, it.fio, it.entryTime, it.minutesInside, it.nextClassName, it.nextClassTime)
        }
    }.mapHttpError()

    override suspend fun scanVisit(token: String, force: Boolean): Result<ScanResult> = runCatching {
        val r = api.scanVisit(ScanRequestDto(token = token, force = force))
        val action = when (r.action) {
            "entered" -> ScanAction.ENTERED
            "warn_quick_exit" -> ScanAction.WARN_QUICK_EXIT
            else -> ScanAction.EXITED
        }
        ScanResult(action = action, fio = r.fio)
    }.mapHttpError()

    override suspend fun getClients(): Result<List<AdminClientInfo>> = runCatching {
        api.getAdminClients().map {
            AdminClientInfo(
                id = it.id,
                name = it.fio,
                cardEndDate = runCatching { LocalDate.parse(it.cardEndDate) }.getOrNull()
            )
        }
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
