package com.example.socialmediamobieapp.model.dto.response

data class RoleResponse(
    val name:String,
    val description:String,
    val permissions:Set<PermissionResponse>
)
