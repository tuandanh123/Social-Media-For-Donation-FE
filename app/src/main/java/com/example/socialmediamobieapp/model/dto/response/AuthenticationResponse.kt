package com.example.socialmediamobieapp.model.dto.response

data class AuthenticationResponse(
    val token:String,
    val authenticated:Boolean,
    val otpRequired:Boolean,
    val message:String
)
