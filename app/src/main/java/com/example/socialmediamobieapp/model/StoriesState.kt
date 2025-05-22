package com.example.socialmediamobieapp.model

import com.example.socialmediamobieapp.network.dto.response.Story

sealed class StoriesState {
    object Loading : StoriesState()
    data class Success(
        val myStory: Story?,
        val userStories: List<Story>
    ) : StoriesState()
    data class Error(val message: String) : StoriesState()
}