package com.example.socialmediamobieapp.model

data class Notification(
    val id: String,
    val userId: String,
    val senderId: String,
    val avatarUrlOfSender: String?,
    val firstNameOfSender: String?,
    val lastNameOfSender: String?,
    val username: String?,
    val createdAt: String?,
    val content: String?,
    val isRead: Boolean,
    val readAt: String?
)