package com.example.fitnessapp.domain.repository

import com.example.fitnessapp.domain.model.Booking

interface BookingRepository {
    suspend fun getAllBookings(): Result<List<Booking>>
    suspend fun getMyBookings(): Result<List<Booking>>
    suspend fun getBookingById(id: Long): Result<Booking>
    suspend fun searchBookings(query: String): Result<List<Booking>>
    suspend fun joinBooking(bookingId: Long): Result<Unit>
    suspend fun leaveBooking(bookingId: Long): Result<Unit>
}
