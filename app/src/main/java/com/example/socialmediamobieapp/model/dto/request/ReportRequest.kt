package com.example.socialmediamobieapp.model.dto.request

data class ReportRequest(
    val profileId:String,
    val postId:String,
    val message:String
)
