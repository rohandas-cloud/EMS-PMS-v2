package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.AttendanceResponse
import com.example.myapplication.data.model.CalendarDay

class CalendarViewModel : ViewModel() {
    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> get() = _calendarDays

    private val _currentMonthName = MutableLiveData<String>()
    val currentMonthName: LiveData<String> get() = _currentMonthName

    private val _presentCount = MutableLiveData<Int>()
    val presentCount: LiveData<Int> get() = _presentCount

    private val _absentCount = MutableLiveData<Int>()
    val absentCount: LiveData<Int> get() = _absentCount

    private val _weekendCount = MutableLiveData<Int>()
    val weekendCount: LiveData<Int> get() = _weekendCount

    private val _averageWorkingHours = MutableLiveData<String>()
    val averageWorkingHours: LiveData<String> get() = _averageWorkingHours

    private val _selectedAttendance = MutableLiveData<AttendanceResponse?>()
    val selectedAttendance: LiveData<AttendanceResponse?> get() = _selectedAttendance

    // Store attendance history for date click
    private var attendanceHistory: List<AttendanceResponse> = emptyList()
    private var currentMonth: Int = 4
    private var currentYear: Int = 2026

    fun generateCalendar(month: Int, year: Int) {
        currentMonth = month
        currentYear = year
        val days = mutableListOf<CalendarDay>()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month - 1, 1)

        val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

        val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(calendar.time)
        _currentMonthName.value = monthName

        // Add empty days for padding
        for (i in 0 until firstDayOfWeek) {
            days.add(CalendarDay(0, "none", false))
        }

        // Add actual days
        for (i in 1..daysInMonth) {
            days.add(CalendarDay(i, "none", true))
        }

        _calendarDays.value = days
    }

    fun onDateSelected(day: Int) {
        android.util.Log.d("CalendarViewModel", "Date selected: $day/$currentMonth/$currentYear")
        
        // Find attendance record for selected date
        val selectedDateStr = String.format(java.util.Locale.US, "%04d-%02d-%02d", currentYear, currentMonth, day)
        android.util.Log.d("CalendarViewModel", "Looking for date: $selectedDateStr")
        
        val record = attendanceHistory.find { attendance ->
            attendance.date?.contains(selectedDateStr) == true || 
            attendance.date?.contains(String.format("%02d", day)) == true
        }
        
        if (record != null) {
            android.util.Log.d("CalendarViewModel", "Found record: $record")
        } else {
            android.util.Log.d("CalendarViewModel", "No record found for this date")
        }
        
        _selectedAttendance.value = record
    }

    fun setAttendanceData(history: List<AttendanceResponse>?) {
        if (history != null) {
            attendanceHistory = history
            android.util.Log.d("CalendarViewModel", "Attendance history stored: ${history.size} records")
        }
        
        val currentDays = _calendarDays.value ?: return
        if (history == null) return

        var present = 0
        var absent = 0
        val weekend = 0

        val updatedDays = currentDays.map { day ->
            if (day.date == 0) return@map day
            
            val dayStr = String.format(java.util.Locale.US, "%02d", day.date)
            val record = history.find { 
                it.date?.contains(dayStr) == true
            }

            if (record != null) {
                if (record.status?.equals("Present", ignoreCase = true) == true) {
                    present++
                    day.copy(status = "present")
                } else {
                    absent++
                    day.copy(status = "absent")
                }
            } else {
                day
            }
        }

        _calendarDays.value = updatedDays
        _presentCount.value = present
        _absentCount.value = absent
        _weekendCount.value = weekend
    }
}
