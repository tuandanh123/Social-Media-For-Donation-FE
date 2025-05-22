package com.example.socialmediamobieapp.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException

object JwtUtils {
    fun isAdmin(token: String): Boolean {
        return try {
            val decodedJWT = JWT.decode(token)
            val scope = decodedJWT.getClaim("scope").asString()
            scope?.contains("ROLE_ADMIN") ?: false
        } catch (e: JWTDecodeException) {
            false
        }
    }
}