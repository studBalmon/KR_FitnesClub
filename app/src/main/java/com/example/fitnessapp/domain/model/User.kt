package com.example.fitnessapp.domain.model

data class User(
    val id: Long,
    val fio: String,
    val phone: String,
    val email: String,
    val userTypeId: Int
)
