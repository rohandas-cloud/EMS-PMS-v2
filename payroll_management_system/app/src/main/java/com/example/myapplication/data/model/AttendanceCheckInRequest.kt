package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class AttendanceCheckInRequest(
    @SerializedName("empId")
    val empId: String
)
