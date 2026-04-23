package com.example.myapplication.data.model

data class AttendanceResponse(
    val date: String? = null,
    val empId: String? = null,
    val fullName: String? = null,
    val inTime: String? = null,
    val outTime: String? = null,
    val remarks: String? = null,
    val status: String? = null,
    val workingHour: String? = null,
    val message: String? = null
)
