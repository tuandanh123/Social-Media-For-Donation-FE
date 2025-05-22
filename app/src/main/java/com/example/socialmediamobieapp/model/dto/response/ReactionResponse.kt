package com.example.socialmediamobieapp.network.dto.response

import com.example.socialmediamobieapp.enums.ReactionType
import java.time.LocalDateTime

data class ReactionResponse(
    val id: String,
    val postId: String,
    val commentId: String? = null,
    val reactionType: ReactionType,
    val profileId: String,
    val createdAt: String?,
    val updatedAt: String?,
    val action: String? = null
)