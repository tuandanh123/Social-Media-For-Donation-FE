package com.example.socialmediamobieapp.model.dto.request

import com.example.socialmediamobieapp.enums.ReactionType

data class ReactionCreationRequest(
    val postId:String,
    val commentId:String,
    val reactionType: ReactionType
)
