package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.local.SearchHistoryDataStore
import com.example.fitnessapp.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
    private val dataStore: SearchHistoryDataStore
) : SearchHistoryRepository {

    override suspend fun getHistory(): List<String> = dataStore.getHistory()

    override suspend fun addQuery(query: String) = dataStore.addQuery(query)

    override suspend fun clearHistory() = dataStore.clearHistory()
}
