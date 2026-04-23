package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LeaveBalanceItem(
    @SerializedName("empLeaveId") val empLeaveId: String? = null,
    @SerializedName("leaveType") val leaveType: String? = null,
    @SerializedName("remainingLeaves") val remainingLeaves: Double? = 0.0,
    @SerializedName("totalLeaves") val totalLeaves: Double? = 0.0,
    @SerializedName("usedLeaves") val usedLeaves: Double? = 0.0,
    @SerializedName("year") val year: String? = null
)