package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LeaveApplyRequest(
    @SerializedName("empLeaveId", alternate = ["empId"])
    val empLeaveId: String,
    
    @SerializedName("leaveType")
    val leaveType: String,
    
    @SerializedName("description", alternate = ["reason"])
    val description: String,
    
    @SerializedName("noOfDays")
    val noOfDays: Double,
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String,
    
    @SerializedName("leaveDay")
    val leaveDay: String
)