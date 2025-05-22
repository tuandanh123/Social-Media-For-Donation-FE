package com.example.socialmediamobieapp.model.dto.request

import java.math.BigDecimal

data class DonationCreationRequest(
    val postId: String,
    val donorId: String,
    val amount: BigDecimal,
    val message: String,
    val isAnonymous: Boolean,
    val paymentMethod: String
)