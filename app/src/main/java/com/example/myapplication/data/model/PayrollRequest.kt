package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class PayrollRequest(
    @SerializedName("empId")
    val empId: String,
    val month: Int,
    val year: Int
)
