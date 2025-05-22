package com.example.socialmediamobieapp.model.dto.request

data class CommentCreationRequest(
    val postId: String,
    val content: String,
    val parentId:String?
)