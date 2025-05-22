package com.example.socialmediamobieapp.network.dto.response

import com.example.socialmediamobieapp.network.dto.ApiResponse

data class LoginResponse(
    val token: String
)

typealias LoginApiResponse = ApiResponse<LoginResponse>