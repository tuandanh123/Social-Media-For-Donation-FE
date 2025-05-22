package com.example.socialmediamobieapp.model

import com.example.socialmediamobieapp.network.dto.response.PostResponse

data class PostWithUser(
    val post: PostResponse,
    val username: String,
    val avatarUrl: String?,
    val tymCount: Int = 0,    // Số lượng lượt thích
    val commentCount: Int = 0, // Số lượng bình luận
    val shareCount: Int = 0,  // Số lượng chia sẻ
    val isLiked: Boolean = false,  // Trạng thái "liked" của người dùng hiện tại
    val isSaved: Boolean = false
)
