package com.example.socialmediamobieapp.model


import com.example.socialmediamobieapp.network.dto.response.PostResponse

data class PostWithDonations(
    val post: PostResponse,
    val creatorUsername: String,
    val donations: List<DonationWithUser>
)