package com.example.fitnessapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore("theme_prefs")

@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val accentColorKey = stringPreferencesKey("accent_color")

    val isDarkTheme: Flow<Boolean> = context.themeDataStore.data.map { it[darkThemeKey] ?: false }

    val accentColor: Flow<String> = context.themeDataStore.data.map {
        it[accentColorKey] ?: "purple"
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.themeDataStore.edit { it[darkThemeKey] = enabled }
    }

    suspend fun setAccentColor(key: String) {
        context.themeDataStore.edit { it[accentColorKey] = key }
    }
}
