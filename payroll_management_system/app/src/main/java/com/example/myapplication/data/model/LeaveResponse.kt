package com.example.myapplication.data.model

data class LeaveResponse(
    val leaveApplicationId: String? = null,
    val empId: String? = null,
    val leaveType: String? = null,
    val leaveDay: String? = null, // "FULL", "FIRST_HALF", "SECOND_HALF"
    val startDate: String? = null,
    val endDate: String? = null,
    val noOfDays: Int? = null,
    val description: String? = null,
    val status: String? = null, // "APPROVED", "PENDING", "REJECTED"
    val remarks: String? = null,
    val createdOn: String? = null,
    // Legacy fields for backward compatibility
    val id: String? = null,
    val reason: String? = null
)