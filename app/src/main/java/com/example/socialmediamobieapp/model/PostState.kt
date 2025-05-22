package com.example.socialmediamobieapp.model

sealed class PostsState {
    object Loading : PostsState()
    data class Success(val posts: List<PostWithUser>) : PostsState()
    data class Error(val message: String) : PostsState()
}
