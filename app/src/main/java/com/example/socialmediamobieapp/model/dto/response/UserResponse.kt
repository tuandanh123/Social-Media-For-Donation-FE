package com.example.socialmediamobieapp.model.dto.response

data class UserResponse(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val dob: String?,
    val email: String?,
    val blocked: Boolean,
    val provider: String,
    val roles: Set<RoleResponse>
)
