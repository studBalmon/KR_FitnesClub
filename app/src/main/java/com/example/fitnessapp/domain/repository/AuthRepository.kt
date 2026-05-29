package com.example.fitnessapp.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun register(fio: String, phone: String, email: String, password: String): Result<Unit>
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}
