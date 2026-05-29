package com.example.fitnessapp.di

import com.example.fitnessapp.data.repository.AuthRepositoryImpl
import com.example.fitnessapp.data.repository.BookingRepositoryImpl
import com.example.fitnessapp.data.repository.SearchHistoryRepositoryImpl
import com.example.fitnessapp.data.repository.ThemeRepositoryImpl
import com.example.fitnessapp.data.repository.UserRepositoryImpl
import com.example.fitnessapp.domain.repository.AuthRepository
import com.example.fitnessapp.domain.repository.BookingRepository
import com.example.fitnessapp.domain.repository.SearchHistoryRepository
import com.example.fitnessapp.domain.repository.ThemeRepository
import com.example.fitnessapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository

    @Binds @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
