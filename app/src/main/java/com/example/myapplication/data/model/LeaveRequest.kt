package com.example.myapplication.data.model

data class LeaveRequest(
    val empId: String,
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String = "PENDING"
)
