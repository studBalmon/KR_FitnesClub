package com.example.fitnessapp.data.api

import com.example.fitnessapp.data.api.dto.AuthResponse
import com.example.fitnessapp.data.api.dto.BookingDto
import com.example.fitnessapp.data.api.dto.CreateBookingRequest
import com.example.fitnessapp.data.api.dto.CreateBookingResponse
import com.example.fitnessapp.data.api.dto.LoginRequest
import com.example.fitnessapp.data.api.dto.ParticipantDto
import com.example.fitnessapp.data.api.dto.RegisterRequest
import com.example.fitnessapp.data.api.dto.UpdateBookingRequest
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

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest)

    // ── Profile ───────────────────────────────────────────────────────────────
    @GET("users/me/profile")
    suspend fun getProfile(): UserProfileDto

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest)

    // ── Bookings (общие) ──────────────────────────────────────────────────────
    @GET("bookings")
    suspend fun getBookings(): List<BookingDto>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): BookingDto

    @GET("bookings/search")
    suspend fun searchBookings(@Query("q") query: String): List<BookingDto>

    // ── Client bookings ───────────────────────────────────────────────────────
    @GET("bookings/my")
    suspend fun getMyBookings(): List<BookingDto>

    @POST("bookings/{id}/join")
    suspend fun joinBooking(@Path("id") id: Long)

    @DELETE("bookings/{id}/join")
    suspend fun leaveBooking(@Path("id") id: Long)

    // ── Coach bookings ────────────────────────────────────────────────────────
    @GET("bookings/coach")
    suspend fun getCoachBookings(): List<BookingDto>

    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): CreateBookingResponse

    @PATCH("bookings/{id}")
    suspend fun updateBooking(@Path("id") id: Long, @Body request: UpdateBookingRequest)

    @DELETE("bookings/{id}")
    suspend fun deleteBooking(@Path("id") id: Long)

    @GET("bookings/{id}/participants")
    suspend fun getParticipants(@Path("id") id: Long): List<ParticipantDto>
}
