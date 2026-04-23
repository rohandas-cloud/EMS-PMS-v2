package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LeaveBalanceApiItem(
    @SerializedName("empLeaveId") val empLeaveId: String?,
    @SerializedName("leaveType") val leaveType: String?,
    @SerializedName("remainingLeaves") val remainingLeaves: Double?,
    @SerializedName("totalLeaves") val totalLeaves: Double?,
    @SerializedName("usedLeaves") val usedLeaves: Double?,
    @SerializedName("year") val year: Int?
)