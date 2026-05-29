package com.example.fitnessapp.presentation.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val BOOKING_DETAIL = "booking_detail/{bookingId}"

    fun bookingDetail(id: Long) = "booking_detail/$id"
}
