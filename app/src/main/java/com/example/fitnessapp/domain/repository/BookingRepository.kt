package com.example.fitnessapp.domain.repository

import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.domain.model.Participant

interface BookingRepository {

    suspend fun getAllBookings(): Result<List<Booking>>
    suspend fun getMyBookings(): Result<List<Booking>>
    suspend fun getBookingById(id: Long): Result<Booking>
    suspend fun searchBookings(query: String): Result<List<Booking>>
    suspend fun joinBooking(bookingId: Long): Result<Unit>
    suspend fun leaveBooking(bookingId: Long): Result<Unit>

    suspend fun getCoachBookings(): Result<List<Booking>>
    suspend fun createBooking(
        name: String,
        slots: Int,
        extra: String?,
        time: String
    ): Result<Long>

    suspend fun updateBooking(
        id: Long,
        name: String,
        slots: Int,
        extra: String?,
        time: String
    ): Result<Unit>

    suspend fun deleteBooking(id: Long): Result<Unit>
    suspend fun getParticipants(bookingId: Long): Result<List<Participant>>
}
