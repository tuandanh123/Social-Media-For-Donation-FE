package com.example.socialmediamobieapp.model

sealed class CommentsState {
    object Loading : CommentsState()
    data class Success(val comments: List<CommentWithUser>) : CommentsState()
    data class Error(val message: String) : CommentsState()
}