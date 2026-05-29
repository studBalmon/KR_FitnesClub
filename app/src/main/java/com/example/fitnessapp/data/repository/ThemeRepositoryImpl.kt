package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.local.ThemeDataStore
import com.example.fitnessapp.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: ThemeDataStore
) : ThemeRepository {

    override suspend fun isDarkTheme(): Boolean = dataStore.isDarkTheme.first()

    override suspend fun setDarkTheme(enabled: Boolean) = dataStore.setDarkTheme(enabled)
}
