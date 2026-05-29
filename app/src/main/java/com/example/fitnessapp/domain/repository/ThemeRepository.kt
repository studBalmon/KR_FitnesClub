package com.example.fitnessapp.domain.repository

interface ThemeRepository {
    suspend fun isDarkTheme(): Boolean
    suspend fun setDarkTheme(enabled: Boolean)
}
