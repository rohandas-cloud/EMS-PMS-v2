package com.example.myapplication.data.model

/**
 * Dummy Attendance Response - Using JSONPlaceholder API structure
 * This is for testing UI without depending on the actual EMS API
 */
data class DummyAttendanceResponse(
    val id: Int,
    val title: String?,
    val body: String?,
    val userId: Int
) {
    /**
     * Convert to AttendanceResponse for UI compatibility
     */
    fun toAttendanceResponse(): AttendanceResponse {
        // Generate dummy attendance data based on the dummy response
        val date = "2026-04-${String.format("%02d", (id % 28) + 1)}"
        val inTime = "09:${String.format("%02d", (id * 7) % 60)}:00"
        val outTime = "18:${String.format("%02d", (id * 13) % 60)}:00"
        val workingHour = "8.${(id * 3) % 60}"
        val status = if (id % 5 == 0) "Absent" else "Present"
        
        return AttendanceResponse(
            date = date,
            empId = "EMP${userId}",
            inTime = inTime,
            outTime = outTime,
            workingHour = workingHour,
            message = "Dummy attendance data for testing",
            status = status,
            remarks = null
        )
    }
}
