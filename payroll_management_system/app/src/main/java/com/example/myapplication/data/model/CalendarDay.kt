package com.example.myapplication.data.model

data class CalendarDay(
    val date: Int,
    val status: String ="none",
    val isCurrentMonth: Boolean = true
)