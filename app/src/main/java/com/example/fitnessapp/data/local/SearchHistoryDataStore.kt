package com.example.fitnessapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchDataStore: DataStore<Preferences> by preferencesDataStore("search_prefs")

private const val MAX_HISTORY = 10
private val DELIMITER = "|"

@Singleton
class SearchHistoryDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val historyKey = stringPreferencesKey("search_history")

    suspend fun getHistory(): List<String> {
        val raw = context.searchDataStore.data.map { it[historyKey] }.firstOrNull()
        return if (raw.isNullOrBlank()) emptyList()
        else raw.split(DELIMITER).filter { it.isNotBlank() }
    }

    suspend fun addQuery(query: String) {
        if (query.isBlank()) return
        val current = getHistory().toMutableList()
        current.remove(query)
        current.add(0, query)
        val trimmed = current.take(MAX_HISTORY)
        context.searchDataStore.edit { it[historyKey] = trimmed.joinToString(DELIMITER) }
    }

    suspend fun clearHistory() {
        context.searchDataStore.edit { it.remove(historyKey) }
    }
}
