package com.example.fitnessapp.data.api

import com.example.fitnessapp.data.api.dto.AdminCreateUserRequest
import com.example.fitnessapp.data.api.dto.ExtendSubscriptionRequest
import com.example.fitnessapp.data.api.dto.CoachTypeRequest
import com.example.fitnessapp.data.api.dto.WorkoutItemDto
import com.example.fitnessapp.data.api.dto.WorkoutItemRequest
import com.example.fitnessapp.data.api.dto.AdminUpdateUserRequest
import com.example.fitnessapp.data.api.dto.AdminUserDto
import com.example.fitnessapp.data.api.dto.AdminCoachDto
import com.example.fitnessapp.data.api.dto.AdminClientDto
import com.example.fitnessapp.data.api.dto.CoachListDto
import com.example.fitnessapp.data.api.dto.TestDataStatusDto
import com.example.fitnessapp.data.api.dto.TestDataToggleDto
import com.example.fitnessapp.data.api.dto.AuthResponse
import com.example.fitnessapp.data.api.dto.BookingDto
import com.example.fitnessapp.data.api.dto.CoachTypeDto
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

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest)

    // Profile
    @GET("users/me/profile")
    suspend fun getProfile(): UserProfileDto

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest)

    // Bookings (общие)
    @GET("bookings")
    suspend fun getBookings(): List<BookingDto>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): BookingDto

    @GET("bookings/search")
    suspend fun searchBookings(@Query("q") query: String): List<BookingDto>

    // Client bookings
    @GET("bookings/my")
    suspend fun getMyBookings(): List<BookingDto>

    @POST("bookings/{id}/join")
    suspend fun joinBooking(@Path("id") id: Long)

    @DELETE("bookings/{id}/join")
    suspend fun leaveBooking(@Path("id") id: Long)

    // Coach bookings
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

    // Workouts (все роли)
    @GET("workouts")
    suspend fun getWorkouts(): List<WorkoutItemDto>

    // Coaches (все роли, для фильтра — id = coaches.id == booking.coachId)
    @GET("coaches")
    suspend fun getCoaches(): List<CoachListDto>

    // Admin
    @GET("admin/users")
    suspend fun getAdminUsers(): List<AdminUserDto>

    @GET("admin/coach-types")
    suspend fun getCoachTypes(): List<CoachTypeDto>

    @GET("admin/coaches")
    suspend fun getAdminCoaches(): List<AdminCoachDto>

    @GET("admin/clients")
    suspend fun getAdminClients(): List<AdminClientDto>

    @POST("admin/users")
    suspend fun createAdminUser(@Body request: AdminCreateUserRequest)

    @PATCH("admin/users/{id}")
    suspend fun updateAdminUser(@Path("id") id: Long, @Body request: AdminUpdateUserRequest)

    @DELETE("admin/users/{id}")
    suspend fun deleteAdminUser(@Path("id") id: Long)

    @PATCH("admin/clients/{userId}/extend")
    suspend fun extendSubscription(
        @Path("userId") userId: Long,
        @Body request: ExtendSubscriptionRequest
    )

    // Admin: тестовые данные
    @GET("admin/test-data/status")
    suspend fun getTestDataStatus(): TestDataStatusDto

    @POST("admin/test-data/toggle")
    suspend fun toggleTestData(): TestDataToggleDto

    // Admin: workouts
    @GET("admin/workouts")
    suspend fun getAdminWorkouts(): List<WorkoutItemDto>

    @POST("admin/workouts")
    suspend fun createAdminWorkout(@Body request: WorkoutItemRequest)

    @PATCH("admin/workouts/{id}")
    suspend fun updateAdminWorkout(@Path("id") id: Int, @Body request: WorkoutItemRequest)

    @DELETE("admin/workouts/{id}")
    suspend fun deleteAdminWorkout(@Path("id") id: Int)

    // Admin: coach types
    @POST("admin/coach-types")
    suspend fun createCoachType(@Body request: CoachTypeRequest)

    @PATCH("admin/coach-types/{id}")
    suspend fun updateCoachType(@Path("id") id: Int, @Body request: CoachTypeRequest)

    @DELETE("admin/coach-types/{id}")
    suspend fun deleteCoachType(@Path("id") id: Int)
}
