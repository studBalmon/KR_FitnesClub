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

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore("token_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("auth_token")

    suspend fun getToken(): String? =
        context.tokenDataStore.data.map { it[tokenKey] }.firstOrNull()

    suspend fun saveToken(token: String) {
        context.tokenDataStore.edit { it[tokenKey] = token }
    }

    suspend fun clearToken() {
        context.tokenDataStore.edit { it.remove(tokenKey) }
    }
}
