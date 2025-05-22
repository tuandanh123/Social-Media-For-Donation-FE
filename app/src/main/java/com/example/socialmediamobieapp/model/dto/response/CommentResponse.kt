package com.example.socialmediamobieapp.model.dto.response

import java.time.LocalDateTime

data class CommentResponse(
    val id: String,
    val profileId: String,
    val postId: String,
    val parentId: String? = null,
    val content: String,
    val fileIds: List<String>?,
    val createdAt: String?,
    val updatedAt: String?
)
