package com.example.fitnessapp.data.api

import com.example.fitnessapp.data.api.dto.AuthResponse
import com.example.fitnessapp.data.api.dto.BookingDto
import com.example.fitnessapp.data.api.dto.LoginRequest
import com.example.fitnessapp.data.api.dto.RegisterRequest
import com.example.fitnessapp.data.api.dto.UpdateProfileRequest
import com.example.fitnessapp.data.api.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest)

    @GET("users/me/profile")
    suspend fun getProfile(): UserProfileDto

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest)

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): BookingDto

    @GET("bookings")
    suspend fun getBookings(): List<BookingDto>

    @GET("bookings/my")
    suspend fun getMyBookings(): List<BookingDto>

    @GET("bookings/search")
    suspend fun searchBookings(@Query("q") query: String): List<BookingDto>

    @POST("bookings/{id}/join")
    suspend fun joinBooking(@Path("id") id: Long)

    @DELETE("bookings/{id}/join")
    suspend fun leaveBooking(@Path("id") id: Long)
}
