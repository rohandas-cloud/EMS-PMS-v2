package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val message: String?,
    val role: String?,
    @SerializedName("username")
    val email: String?,
    val user: UserInfo?
)

data class UserInfo(
    val userName: String?,
    val email: String?,
    val status: Boolean?,
    val id: String?,
    @SerializedName("employeeId") // MUST match backend JSON key
    val employeeId: String?
)
