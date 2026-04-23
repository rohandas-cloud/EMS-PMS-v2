package com.example.myapplication.data.model

data class MonthlyAttendanceResponse(
    val absentDays: Int,
    val empId: String?,
    val halfDays: Int,
    val holidays: Int,
    val leaveDays: Int,
    val month: Int,
    val presentDays: Int,
    val totalDays: Int,
    val year: Int
)
