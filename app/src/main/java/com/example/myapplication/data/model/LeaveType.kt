package com.example.myapplication.data.model

data class LeaveType(
    val typeId: String,
    val type: String,
    val carryForwardAllowed: Boolean,
    val postApplicationAllowed: Boolean,
    val maxConsecutiveDays: Int,
    val createdBy: String,
    val createdOn: String
)
