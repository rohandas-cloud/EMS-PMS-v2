package com.example.myapplication.data.model


data class UserResponse(
    val id: String, // Ye wo UUID ho sakti hai
    val employeeId: String?, // Ye aapka main ID ho sakta hai
    val username: String?,
    val email: String?
)