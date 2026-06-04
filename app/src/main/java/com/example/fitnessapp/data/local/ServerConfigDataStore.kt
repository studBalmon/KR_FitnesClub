package com.example.fitnessapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitnessapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.serverConfigDataStore: DataStore<Preferences> by preferencesDataStore("server_prefs")

@Singleton
class ServerConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val urlKey = stringPreferencesKey("server_url")

    /** Адрес сервера; по умолчанию — значение из сборки (BuildConfig.BASE_URL). */
    val serverUrl: Flow<String> =
        context.serverConfigDataStore.data.map { it[urlKey] ?: BuildConfig.BASE_URL }

    suspend fun setServerUrl(url: String) {
        context.serverConfigDataStore.edit { it[urlKey] = url }
    }
}
