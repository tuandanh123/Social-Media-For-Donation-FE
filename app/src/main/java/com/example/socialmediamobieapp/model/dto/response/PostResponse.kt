package com.example.socialmediamobieapp.network.dto.response

import com.example.socialmediamobieapp.enums.Privacy
import com.example.socialmediamobieapp.model.Point

data class PostResponse(
    val id: String,
    val profileId: String,
    val content: String,
    val fileIds: List<String>,
    val tags: List<String>,
    val privacy: Privacy,
    val point: Point?,
    val donationStartTime: String?, // LocalDateTime được chuyển thành chuỗi ISO
    val donationEndTime: String?,   // LocalDateTime được chuyển thành chuỗi ISO
    val createdAt: String,          // LocalDateTime được chuyển thành chuỗi ISO
    val updatedAt: String           // LocalDateTime được chuyển thành chuỗi ISO
)