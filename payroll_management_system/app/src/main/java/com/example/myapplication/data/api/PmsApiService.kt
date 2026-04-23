package com.example.myapplication.data.api

import com.example.myapplication.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * PMS (Payroll Management System) API Service
 * Handles all PMS-specific endpoints
 */
interface PmsApiService {

    // AUTH
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // ATTENDANCE
    @GET("api/attendance/history/{empId}")
    suspend fun getAttendanceHistory(@Path("empId") empId: String): Response<List<AttendanceResponse>>

    @POST("api/attendance/mark")
    suspend fun markAttendance(@Body request: AttendanceCheckInRequest): Response<AttendanceResponse>

    // LEAVES
    @GET("api/leaves/balance/{empId}")
    suspend fun getLeaveBalance(@Path("empId") empId: String): Response<LeaveBalanceResponse>

    @GET("api/leaves/history")
    suspend fun getLeaveHistory(): Response<List<LeaveResponse>>

    @GET("api/leaves/types")
    suspend fun getLeaveTypes(): Response<List<LeaveType>>

    @POST("api/leaves/apply")
    suspend fun applyLeave(@Body request: LeaveApplyRequest): Response<LeaveApplyResponse>

    // HOLIDAYS
    @GET("api/holidays")
    suspend fun getHolidays(): Response<List<Holiday>>

    // PAYROLL
    @GET("api/salary/details/{empId}")
    suspend fun getPayrollDetails(@Path("empId") empId: String): Response<PayrollDetailsResponse>

    // ✅ MATCHES: GET /api/salary?empId=xxx&month=4&year=2026
    @GET("api/salary")
    suspend fun getPayrollByMonthYear(
        @Query("empId") empId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<PayrollDetailsResponse>

    @POST("api/salary/details")
    suspend fun getPayrollDetailsForMonthYear(@Body request: PayrollRequest): Response<PayrollDetailsResponse>

    // 3.3 Download Payslip as PDF
    @GET("api/salary/pdf")
    suspend fun downloadPayslipPdf(
        @Query("empId") empId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ResponseBody>

    // 3.4 Get Payroll Structure (Earnings + Deductions)
    @GET("api/pay-structures")
    suspend fun getPayrollStructures(): Response<List<PayrollStructure>>

    // 3.5 Get Salary Components
    @GET("api/salary-components")
    suspend fun getSalaryComponents(): Response<List<SalaryComponentInfo>>

}
