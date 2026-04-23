package com.example.myapplication.data.model

data class LeaveBalanceResponse(
    val empId: String? = null,
    val casualLeave: Int? = null,
    val sickLeave: Int? = null,
    val earnedLeave: Int? = null,
    val totalLeave: Int? = null,
    val message: String? = null
)