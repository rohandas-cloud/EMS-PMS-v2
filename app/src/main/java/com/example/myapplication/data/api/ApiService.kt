package com.example.myapplication.data.api
//
//import com.example.myapplication.data.model.*
//import okhttp3.ResponseBody
//import retrofit2.Response
//import retrofit2.http.*
//
//interface ApiService {
//
//    // AUTH
//    @POST("auth/login")
//    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
//
//    // USER
//    @GET("api/users/{userId}/employee")
//    suspend fun getEmployeeId(
//        @Path("userId") userId: String,
//        @Header("Authorization") token: String
//    ): Response<String>
//
//    @GET("api/employee/profile")
//    suspend fun getEmployeeProfile(
//        @Header("Authorization") token: String
//    ): Response<EmployeeProfileResponse>
//
//    // ATTENDANCE
//    @GET("api/attendance/history")
//    suspend fun getAttendanceHistory(
//        @Header("Authorization") token: String,
//        @Query("empId") empId: String
//    ): Response<List<AttendanceResponse>>
//
//    @POST("api/attendance")
//    suspend fun markAttendance(
//        @Header("Authorization") token: String,
//        @Body request: AttendanceCheckInRequest
//    ): Response<AttendanceResponse>
//
//    // LEAVE
//    @GET("api/leaves/balance")
//    suspend fun getLeaveBalance(
//        @Header("Authorization") token: String,
//        @Query("empId") empId: String
//    ): Response<LeaveBalanceResponse>
//
//    @GET("api/leaves/types")
//    suspend fun getLeaveTypes(
//        @Header("Authorization") token: String
//    ): Response<List<LeaveType>>
//
//    @GET("api/leaves/history")
//    suspend fun getLeaveHistory(
//        @Header("Authorization") token: String
//    ): Response<List<LeaveResponse>>
//
//    @POST("api/leaves/apply")
//    suspend fun applyLeave(
//        @Header("Authorization") token: String,
//        @Body request: LeaveApplyRequest
//    ): Response<Map<String, Any>>
//
//    // PAYROLL
//    @POST("api/salary/get")
//    suspend fun getPayrollDetails(
//        @Header("Authorization") token: String,
//        @Body request: PayrollRequest
//    ): Response<PayrollResponse>
//
//    @POST("api/salary/summary")
//    suspend fun getPayrollSummary(
//        @Body request: PayrollSummaryRequest
//    ): Response<PayrollSummaryResponse>
//
//    @GET("api/salary/detail/{empSalaryId}")
//    suspend fun getPayrollDetail(
//        @Path("empSalaryId") empSalaryId: String
//    ): Response<PayrollDetailResponse>
//
//    @GET("api/salary/download/{empSalaryId}")
//    suspend fun downloadPayslip(
//        @Path("empSalaryId") empSalaryId: String
//    ): Response<ResponseBody>
//
//    @GET("api/salary/download")
//    suspend fun downloadPayslipByMonth(
//        @Query("empId") empId: String,
//        @Query("month") month: Int,
//        @Query("year") year: Int
//    ): Response<ResponseBody>
//
//    @GET("api/holidays")
//    suspend fun getHolidays(): Response<List<Holiday>>
//}