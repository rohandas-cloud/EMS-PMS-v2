package com.example.myapplication.data.api

    import com.example.myapplication.data.model.*
    import okhttp3.ResponseBody
    import retrofit2.Response
    import retrofit2.http.*

    /**
     * EMS (Employee Management System) API Service
     * Handles all EMS-specific endpoints
     */
    interface EmsApiService {

        // AUTH
        @POST("auth/login")
        suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>


        // ====================
        // ATTENDANCE MODULE
        // ====================

        // 1.1 Check-in / Check-out
        @POST("api/attendance")
        suspend fun markAttendance(
            @Body request: AttendanceCheckInRequest
        ): Response<AttendanceResponse>

        // 1.2 Attendance History (Individual)
        @GET("api/attendance/history")
        suspend fun getAttendanceHistory(
            @Query("empId") empId: String
        ): Response<List<AttendanceResponse>>

        // 1.3 Today Attendance (Summary of all employees)
        @GET("api/attendance/today")
        suspend fun getTodayAttendanceList(
        ): Response<List<AttendanceResponse>>

        // 1.4 Monthly Attendance Summary
        @GET("api/attendance/monthly")
        suspend fun getMonthlyAttendance(
            @Query("empId") empId: String,
            @Query("year") year: Int,
            @Query("month") month: Int
        ): Response<MonthlyAttendanceResponse>

        // 1.5 Attendance Summary by Date
        @GET("api/attendance/all")
        suspend fun getAttendanceSummary(
            @Query("date") date: String
        ): Response<List<AttendanceResponse>>

        // 1.6 Daily Attendance Details
        @GET("api/attendance/daily")
        suspend fun getDailyAttendance(
            @Query("empId") empId: String,
            @Query("date") date: String
        ): Response<AttendanceResponse>

        // 1.7 Holiday Fetch
        @GET("api/holidays")
        suspend fun getHolidays(): Response<List<HolidayResponse>>

        // ====================
        // LEAVE MODULE
        // ====================

        // 2.1 Leave Types (Dropdown)
        @GET("api/leaveTypes")
        suspend fun getLeaveTypes(
        ): Response<List<LeaveType>>

        // 2.2 Apply Leave (Primary)
        @POST("api/employee-leaves/allocate")
        suspend fun applyLeave(
            @Body request: LeaveApplyRequest
        ): Response<LeaveApplyResponse>

        // 2.2.1 Apply Leave (Alternative/Standard)
        @POST("api/leaves/apply")
        suspend fun applyLeaveStandard(
            @Body request: LeaveApplyRequest
        ): Response<LeaveApplyResponse>

        // 2.3 Leave History
        @GET("api/leaves/history")
        suspend fun getLeaveHistory(
            @Query("empId") empId: String
        ): Response<List<LeaveResponse>>

        // 2.4 Leave Balance
        @GET("api/leaves/balance/{empId}")
        suspend fun getLeaveBalance(@Path("empId") empId: String
        ): Response<List<LeaveBalanceItem>>

        // 2.5 Leave Requests (HR)
        @GET("api/leaves/requests")
        suspend fun getLeaveRequests(
            @Query("status") status: String? = null
        ): Response<List<LeaveResponse>>

        // 2.7 Approve / Reject Leave
        @POST("api/leaves/approve-reject")
        suspend fun approveRejectLeave(
            @Body request: LeaveApprovalRequest
        ): Response<LeaveApprovalResponse>

        // 2.8 Single Leave Details
        @GET("api/leaves/{leaveApplicationId}")
        suspend fun getLeaveDetails(
            @Path("leaveApplicationId") leaveApplicationId: String
        ): Response<LeaveResponse>

        // ====================
        // PAYROLL/SALARY MODULE
        // ====================
        
        // Get Payroll by Month/Year
        @GET("api/salary")
        suspend fun getPayrollByMonthYear(
            @Query("empId") empId: String,
            @Query("month") month: Int,
            @Query("year") year: Int
        ): Response<PayrollDetailsResponse>
    }
