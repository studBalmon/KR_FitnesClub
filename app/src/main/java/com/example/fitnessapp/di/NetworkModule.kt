package com.example.fitnessapp.di

import com.example.fitnessapp.data.api.ApiService
import com.example.fitnessapp.data.api.AuthInterceptor
import com.example.fitnessapp.data.api.BaseUrlInterceptor
import com.example.fitnessapp.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideOkHttpClient(
        baseUrlInterceptor: BaseUrlInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(baseUrlInterceptor)   // подменяет адрес сервера на актуальный
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
