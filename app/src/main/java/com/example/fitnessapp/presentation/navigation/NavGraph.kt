package com.example.fitnessapp.presentation.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val COACH_MAIN = "coach_main"
    const val ADMIN_MAIN = "admin_main"
    const val BOOKING_DETAIL = "booking_detail/{bookingId}"
    const val CREATE_BOOKING = "create_booking"
    const val EDIT_BOOKING = "edit_booking/{bookingId}"
    const val PARTICIPANTS = "participants/{bookingId}"

    fun bookingDetail(id: Long) = "booking_detail/$id"
    fun editBooking(id: Long) = "edit_booking/$id"
    fun participants(bookingId: Long) = "participants/$bookingId"
}
