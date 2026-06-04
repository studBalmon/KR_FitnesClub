package com.example.fitnessapp.data.api

import com.example.fitnessapp.BuildConfig
import com.example.fitnessapp.data.local.ServerConfigDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerUrlProvider @Inject constructor(
    private val store: ServerConfigDataStore
) {
    @Volatile
    var baseUrl: String =
        runBlocking { runCatching { store.serverUrl.first() }.getOrDefault(BuildConfig.BASE_URL) }
        private set

    suspend fun setBaseUrl(raw: String) {
        val normalized = normalize(raw)
        store.setServerUrl(normalized)
        baseUrl = normalized
    }

    companion object {

        fun normalize(raw: String): String {
            var s = raw.trim()
            if (s.isEmpty()) return BuildConfig.BASE_URL
            if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://$s"
            if (!s.endsWith("/")) s = "$s/"
            return s
        }
    }
}
