package com.example.socialmediamobieapp.network.dto.request

data class SignUpRequest(
    val username: String,
    val password: String,
    val firstname: String,
    val lastname: String,
    val dob: String,     // ISO “yyyy-MM-dd”
    val email: String
)