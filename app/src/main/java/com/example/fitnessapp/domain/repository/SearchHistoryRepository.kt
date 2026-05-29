package com.example.fitnessapp.domain.repository

interface SearchHistoryRepository {
    suspend fun getHistory(): List<String>
    suspend fun addQuery(query: String)
    suspend fun clearHistory()
}
