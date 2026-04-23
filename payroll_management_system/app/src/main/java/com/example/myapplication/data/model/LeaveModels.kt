package com.example.myapplication.data.model

data class LeaveApplyResponse(
    val leaveApplicationId: String?,
    val empId: String?,
    val status: String?,
    val message: String?
)

data class LeaveApprovalRequest(
    val leaveApplicationId: String,
    val status: String, // "APPROVED" or "REJECTED"
    val remarks: String?
)

data class LeaveApprovalResponse(
    val leaveApplicationId: String?,
    val status: String?,
    val remarks: String?,
    val message: String?
)
