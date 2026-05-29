package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.api.ApiService
import com.example.fitnessapp.data.api.dto.LoginRequest
import com.example.fitnessapp.data.api.dto.RegisterRequest
import com.example.fitnessapp.data.local.TokenDataStore
import com.example.fitnessapp.domain.repository.AuthRepository
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> = runCatching {
        val response = api.login(LoginRequest(email, password))
        tokenDataStore.saveToken(response.token)
        response.token
    }.mapHttpError()

    override suspend fun register(fio: String, phone: String, email: String, password: String): Result<Unit> = runCatching {
        api.register(RegisterRequest(fio = fio, phone = phone, email = email, password = password))
    }.mapHttpError()

    override suspend fun getToken(): String? = tokenDataStore.getToken()

    override suspend fun saveToken(token: String) = tokenDataStore.saveToken(token)

    override suspend fun clearToken() = tokenDataStore.clearToken()
}

private fun <T> Result<T>.mapHttpError(): Result<T> = recoverCatching { e ->
    if (e is HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val message = try {
            Gson().fromJson(errorBody, Map::class.java)["error"] as? String
        } catch (_: Exception) { null }
        throw Exception(message ?: "Ошибка сервера (${e.code()})")
    }
    throw e
}
