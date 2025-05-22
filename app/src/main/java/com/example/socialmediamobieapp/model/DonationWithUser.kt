package com.example.socialmediamobieapp.model

import com.example.socialmediamobieapp.model.dto.response.DonationResponse

data class DonationWithUser(
    val donation: DonationResponse,
    val username: String,
    val avatarUrl: String?
)
