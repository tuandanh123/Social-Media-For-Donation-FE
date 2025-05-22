package com.example.socialmediamobieapp.network.dto.response

import com.example.socialmediamobieapp.model.User
import com.example.socialmediamobieapp.network.dto.ApiResponse
import java.time.LocalDate


data class SignUpResponse(
    val user: User
)

typealias SignUpApiResponse = ApiResponse<SignUpResponse>