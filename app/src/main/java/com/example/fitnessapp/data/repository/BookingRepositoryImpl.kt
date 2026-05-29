package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.api.ApiService
import com.example.fitnessapp.data.api.dto.toDomain
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.repository.BookingRepository
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val api: ApiService
) : BookingRepository {

    override suspend fun getAllBookings(): Result<List<Booking>> = runCatching {
        api.getBookings().map { it.toDomain() }
    }

    override suspend fun getMyBookings(): Result<List<Booking>> = runCatching {
        api.getMyBookings().map { it.toDomain() }
    }

    override suspend fun getBookingById(id: Long): Result<Booking> = runCatching {
        api.getBookingById(id).toDomain()
    }

    override suspend fun searchBookings(query: String): Result<List<Booking>> = runCatching {
        api.searchBookings(query).map { it.toDomain() }
    }

    override suspend fun joinBooking(bookingId: Long): Result<Unit> = runCatching {
        api.joinBooking(bookingId)
    }.mapHttpError()

    override suspend fun leaveBooking(bookingId: Long): Result<Unit> = runCatching {
        api.leaveBooking(bookingId)
    }.mapHttpError()
}

private fun <T> Result<T>.mapHttpError(): Result<T> = recoverCatching { e ->
    if (e is HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val message = try {
            Gson().fromJson(errorBody, Map::class.java)["error"] as? String
        } catch (_: Exception) { null }
        throw Exception(message ?: "Ошибка сервера (${e.code()})")
    }
    throw e
}
