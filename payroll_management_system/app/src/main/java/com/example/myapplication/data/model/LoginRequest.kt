package com.example.myapplication.data.model

data class LoginRequest(
    val email: String,
    val password: String,
    val username: String? = null
)
