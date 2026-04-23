package com.example.myapplication.data.model

data class LeaveApplyRequest(
    val empId: String,
    val leaveType: String,
    val reason: String,
    val noOfDays: Double,
    val startDate: String,
    val endDate: String,
    val leaveDay: String
)