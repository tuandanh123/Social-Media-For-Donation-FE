package com.example.socialmediamobieapp.network.dto.response

import com.example.socialmediamobieapp.enums.Privacy
import com.example.socialmediamobieapp.model.Point

data class ProfileResponse(
    val profileId: String,
    val userId: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String,
    val location: String,
    val bio: String, // LocalDateTime được chuyển thành chuỗi ISO
    val createdAt: String,          // LocalDateTime được chuyển thành chuỗi ISO
    val updatedAt: String           // LocalDateTime được chuyển thành chuỗi ISO
)