package com.example.plugins.Auth

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


@Serializable
data class UserSession(val userId: String, val username: String) : Principal

@Serializable
data class LoginRequest(val userId: String, val username: String)
@Serializable
data class oldToken(val oldToken: String)

