package com.example.socialmediamobieapp.model.dto.response

import java.math.BigDecimal

data class DonationResponse(
    val id: String,
    val postId: String,
    val donorId: String,
    val amount: BigDecimal,
    val message: String,
    val isAnonymous: Boolean,
    val status: String, // Giả sử DonationStatus là enum hoặc String
    val paymentMethod: String,
    val paymentRefId: String,
    val createdAt: String, // Hoặc LocalDateTime nếu dùng JSON parser phù hợp
    val paidAt: String?,
    val payUrl: String
)