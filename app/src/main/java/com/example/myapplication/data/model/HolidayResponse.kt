package com.example.myapplication.data.model

data class HolidayResponse(
    val holidayId: String?,
    val holidayName: String?,
    val holidayDate: String?,
    val holidayType: String?,
    val calendarYear: Int?,
    val isActive: Boolean?,
    val createdBy: String?,
    val createdOn: String?,
    val updatedBy: String?,
    val updatedOn: String?
)
