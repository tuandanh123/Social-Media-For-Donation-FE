package com.example.socialmediamobieapp.model

import java.time.LocalDate

data class User(
    val id: String,
    val username: String,
    val firstname: String,
    val lastname: String,
    val dob: LocalDate,   // hoặc Date, tùy thuộc vào loại dữ liệu bạn muốn sử dụng
    val email: String
)