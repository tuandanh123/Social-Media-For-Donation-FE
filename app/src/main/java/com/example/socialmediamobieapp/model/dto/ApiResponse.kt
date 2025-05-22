package com.example.socialmediamobieapp.network.dto

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val result: T?   // T có thể là bất kỳ dữ liệu nào được trả về từ backend
)