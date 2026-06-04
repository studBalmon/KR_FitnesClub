package com.example.fitnessapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

@Singleton
class UserDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val userTypeKey = intPreferencesKey("user_type_id")

    suspend fun getUserTypeId(): Int =
        context.userDataStore.data.map { it[userTypeKey] ?: 3 }.firstOrNull() ?: 3

    suspend fun saveUserTypeId(id: Int) {
        context.userDataStore.edit { it[userTypeKey] = id }
    }

    suspend fun clear() {
        context.userDataStore.edit { it.remove(userTypeKey) }
    }
}
