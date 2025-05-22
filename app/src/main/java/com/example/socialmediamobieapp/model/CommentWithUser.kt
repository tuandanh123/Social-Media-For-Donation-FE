package com.example.socialmediamobieapp.model

import com.example.socialmediamobieapp.model.dto.response.CommentResponse

data class CommentWithUser(
    val comment: CommentResponse,
    val username: String,
    val avatarUrl: String?,
    val likeCount: Int,
    val replies: List<CommentWithUser> = emptyList() // Thêm danh sách replies)
)