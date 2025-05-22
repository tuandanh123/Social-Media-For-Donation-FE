package com.example.socialmediamobieapp.network.dto

data class PageResponse<T>(
    val currentPage:Int,
    val totalPages:Int,
    val pageSize:Int,
    val totalElements:Long,
    val data : List<T>
)