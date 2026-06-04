package com.example.fitnessapp.data.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BaseUrlInterceptor @Inject constructor(
    private val provider: ServerUrlProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val configured = provider.baseUrl.toHttpUrlOrNull()
            ?: return chain.proceed(chain.request())
        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .scheme(configured.scheme)
            .host(configured.host)
            .port(configured.port)
            .build()
        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}
