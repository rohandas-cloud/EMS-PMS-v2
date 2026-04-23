package com.example.myapplication.data.model

data class AttendanceResponse(
    val date: String?,
    val empId: String?,
    val inTime: String?,
    val outTime: String?,
    val remarks: String?,
    val status: String?,
    val workingHour: String?,
    val message: String? = null
)
