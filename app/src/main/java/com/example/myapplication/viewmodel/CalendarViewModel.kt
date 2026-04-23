package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.AttendanceResponse
import com.example.myapplication.data.model.CalendarDay
import java.text.SimpleDateFormat
import java.util.*

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

    private val _selectedDateString = MutableLiveData<String>()
    val selectedDateString: LiveData<String> get() = _selectedDateString

    private var attendanceHistory: List<AttendanceResponse> = emptyList()
    private val fetchedRecords = mutableMapOf<String, AttendanceResponse>()
    var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    fun generateCalendar(month: Int, year: Int) {
        currentMonth = month
        currentYear = year
        val days = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)

        val firstDay = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        _currentMonthName.value = monthName

        // Monday-start logic: (Mon=0...Sun=6)
        val startOffset = (firstDay + 5) % 7

        for (i in 0 until startOffset) {
            days.add(CalendarDay(date = 0, status = "none", isCurrentMonth = false, isWeekend = false))
        }
        for (i in 1..daysInMonth) {
            val cal = Calendar.getInstance()
            cal.set(year, month - 1, i)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val weekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            days.add(CalendarDay(date = i, status = "none", isCurrentMonth = true, isWeekend = weekend))
        }
        _calendarDays.value = days
        
        if (attendanceHistory.isNotEmpty() || fetchedRecords.isNotEmpty()) {
            setAttendanceData(attendanceHistory)
        }
    }

    fun onDateSelected(day: Int) {
        if (day == 0) return
        val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth, day)
        _selectedDateString.value = dateStr
        
        // Also try to find it in history immediately for faster UI (optional)
        val record = attendanceHistory.find { 
            val apiDate = it.date?.trim() ?: ""
            apiDate == dateStr || apiDate.startsWith(dateStr)
        } ?: fetchedRecords[dateStr]
        _selectedAttendance.value = record
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDateString.value = dateStr
        
        val record = attendanceHistory.find { 
            val apiDate = it.date?.trim() ?: ""
            apiDate == dateStr || apiDate.startsWith(dateStr)
        } ?: fetchedRecords[dateStr]
        _selectedAttendance.value = record
        
        // Update Calendar Month if the selected date is in a different month
        try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                if (currentMonth != month || currentYear != year) {
                    currentMonth = month
                    currentYear = year
                    generateCalendar(currentMonth, currentYear)
                }
            }
        } catch (e: Exception) {
            Log.e("CalendarViewModel", "Failed to parse date string: $dateStr", e)
        }
    }

    fun updateTodayRecord(record: AttendanceResponse?) {
        if (record?.date != null) {
            fetchedRecords[record.date.trim()] = record
        }
        setAttendanceData(attendanceHistory)
    }

    fun setAttendanceData(history: List<AttendanceResponse>?) {
        val safeHistory = history ?: emptyList()
        
        // If history is empty and we have no fetched records, inject dummy data for UI demo
        val effectiveHistory = if (safeHistory.isEmpty() && fetchedRecords.isEmpty()) {
            getDummyAttendanceHistory(currentMonth, currentYear)
        } else {
            safeHistory
        }
        
        attendanceHistory = effectiveHistory

        val currentDays = _calendarDays.value ?: return
        
        // Merge fetched records into history for calculation
        val fullHistoryMap = effectiveHistory.associateBy { it.date?.trim() ?: "" }.toMutableMap()
        for ((k, v) in fetchedRecords) {
            fullHistoryMap[k] = v
        }
        val fullHistory = fullHistoryMap.values.toList()

        var present = 0
        var absent = 0
        var weekendCount = 0
        var totalSeconds = 0L
        var daysWithHours = 0

        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val todayMonth = today.get(Calendar.MONTH) + 1
        val todayYear = today.get(Calendar.YEAR)

        val updatedDays = currentDays.map { day ->
            if (day.date == 0) return@map day
            
            // Weekend logic
            if (day.isWeekend) {
                return@map day.copy(status = "weekend")
            }
            
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth, day.date)
            
            val record = fullHistory.find { 
                val apiDate = it.date?.trim() ?: ""
                apiDate == dateStr || apiDate.startsWith(dateStr)
            }

            // Determine if the day is in the past
            val isPast = when {
                currentYear < todayYear -> true
                currentYear > todayYear -> false
                else -> when {
                    currentMonth < todayMonth -> true
                    currentMonth > todayMonth -> false
                    else -> day.date < todayDay
                }
            }

            if (record != null) {
                // Parse working hours if present
                record.workingHour?.let { timeStr ->
                    val seconds = parseTimeToSeconds(timeStr)
                    if (seconds > 0) {
                        totalSeconds += seconds
                        daysWithHours++
                    }
                }

                val isPresent = record.status?.contains("PRESENT", ignoreCase = true) == true || !record.inTime.isNullOrBlank()

                when {
                    isPresent -> {
                        present++
                        day.copy(status = "present")
                    }
                    record.status?.contains("ABSENT", ignoreCase = true) == true -> {
                        absent++
                        day.copy(status = "absent")
                    }
                    record.status?.contains("WEEKEND", ignoreCase = true) == true -> {
                        weekendCount++
                        day.copy(status = "weekend")
                    }
                    else -> day.copy(status = "none")
                }
            } else {
                // Past days with NO record = Absent (Red Dot)
                if (isPast) {
                    if (day.isWeekend) {
                        weekendCount++
                        day.copy(status = "weekend")
                    } else {
                        absent++
                        day.copy(status = "absent")
                    }
                } else {
                    day
                }
            }
        }

        _calendarDays.value = updatedDays
        _presentCount.value = present
        _absentCount.value = absent
        _weekendCount.value = weekendCount
        
        // Calculate Average
        if (daysWithHours > 0) {
            val avgSeconds = totalSeconds / daysWithHours
            _averageWorkingHours.value = formatSecondsToTime(avgSeconds)
        } else {
            _averageWorkingHours.value = "0h 0m"
        }
    }


    private fun getDummyAttendanceHistory(month: Int, year: Int): List<AttendanceResponse> {
        val dummyList = mutableListOf<AttendanceResponse>()
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val todayMonth = today.get(Calendar.MONTH) + 1
        val todayYear = today.get(Calendar.YEAR)

        for (i in 1..daysInMonth) {
            val cal = Calendar.getInstance()
            cal.set(year, month - 1, i)
            
            // Only generate for past days or today
            val isPastOrToday = when {
                year < todayYear -> true
                year > todayYear -> false
                else -> when {
                    month < todayMonth -> true
                    month > todayMonth -> false
                    else -> i <= todayDay
                }
            }
            
            if (!isPastOrToday) continue

            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                dummyList.add(AttendanceResponse(
                    date = String.format(Locale.US, "%04d-%02d-%02d", year, month, i),
                    status = "WEEKEND",
                    inTime = null,
                    outTime = null,
                    workingHour = null
                ))
                continue
            }

            // 80% chance of being present
            val isPresent = (1..100).random() <= 85
            if (isPresent) {
                dummyList.add(AttendanceResponse(
                    date = String.format(Locale.US, "%04d-%02d-%02d", year, month, i),
                    status = "PRESENT",
                    inTime = "09:${(10..45).random()}:00",
                    outTime = "18:${(10..55).random()}:00",
                    workingHour = "09:${(0..30).random()}:00"
                ))
            } else {
                dummyList.add(AttendanceResponse(
                    date = String.format(Locale.US, "%04d-%02d-%02d", year, month, i),
                    status = "ABSENT",
                    inTime = null,
                    outTime = null,
                    workingHour = null
                ))
            }
        }
        return dummyList
    }

    private fun parseTimeToSeconds(timeStr: String): Long {
        return try {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val hours = parts[0].toLong()
                val minutes = parts[1].toLong()
                val seconds = if (parts.size > 2) parts[2].toLong() else 0L
                (hours * 3600) + (minutes * 60) + seconds
            } else 0L
        } catch (e: Exception) {
            Log.e("CalendarViewModel", "Error parsing time", e)
            0L
        }
    }

    private fun formatSecondsToTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return "${h}h ${m}m"
    }
}
