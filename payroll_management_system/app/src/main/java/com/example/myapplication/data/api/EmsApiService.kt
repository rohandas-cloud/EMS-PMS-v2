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

        // 1.2 Attendance History
        @GET("api/attendance/employee/{empId}")
        suspend fun getAttendanceHistory(
            @Path("empId") empId: String
        ): Response<List<AttendanceResponse>>

        // 1.3 Today Attendance
        @GET("api/attendance/today")
        suspend fun getTodayAttendance(
        ): Response<AttendanceResponse>

        // 1.4 Monthly Attendance
        @GET("api/attendance/monthly")
        suspend fun getMonthlyAttendance(
            @Query("empId") empId: String,
            @Query("year") year: Int,
            @Query("month") month: Int
        ): Response<List<AttendanceResponse>>

        // ====================
        // LEAVE MODULE
        // ====================

        // 2.1 Leave Types (Dropdown)
        @GET("api/leaveTypes")
        suspend fun getLeaveTypes(
        ): Response<List<LeaveType>>

        // 2.2 Apply Leave
        @POST("api/leaves/apply")
        suspend fun applyLeave(
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
        ): Response<List<LeaveBalanceApiItem>>

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
