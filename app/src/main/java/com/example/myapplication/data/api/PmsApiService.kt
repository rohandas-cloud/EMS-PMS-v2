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

    // 1.0 Login
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // 1.1 Refresh Token
    @POST("api/auth/refresh")
    suspend fun refreshAuthToken(): Response<TokenRefreshResponse>

    // 1.2 Logout
    @POST("api/auth/logout")
    suspend fun logoutUser(): Response<ResponseBody>

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

    @GET("api/salary")
    suspend fun getPayrollByMonthYear(
        @Query("empId") empId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<PayrollDetailsResponse>

    @POST("api/salary/details")
    suspend fun getPayrollDetailsForMonthYear(@Body request: PayrollRequest): Response<PayrollDetailsResponse>

    @GET("api/salary/pdf")
    suspend fun downloadPayslipPdf(
        @Query("empId") empId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ResponseBody>

    // --- REVISION TYPES ---
    @GET("api/revision-types")
    suspend fun getRevisionTypes(): Response<List<RevisionType>>

    @POST("api/revision-types")
    suspend fun createRevisionType(@Body request: RevisionType): Response<RevisionType>

    @PUT("api/revision-types/{id}")
    suspend fun updateRevisionType(@Path("id") id: String, @Body request: RevisionType): Response<RevisionType>

    @DELETE("api/revision-types/{id}")
    suspend fun deleteRevisionType(@Path("id") id: String): Response<ResponseBody>

    // --- PAYROLL CYCLES ---
    @GET("api/payroll-cycles")
    suspend fun getPayrollCycles(): Response<List<PayrollCycle>>

    @POST("api/payroll-cycles")
    suspend fun createPayrollCycle(@Body request: PayrollCycle): Response<PayrollCycle>

    @PUT("api/payroll-cycles/{id}")
    suspend fun updatePayrollCycle(@Path("id") id: String, @Body request: PayrollCycle): Response<PayrollCycle>

    @DELETE("api/payroll-cycles/{id}")
    suspend fun deletePayrollCycle(@Path("id") id: String): Response<ResponseBody>

    // --- TAX SLABS ---
    @GET("api/tax-slabs")
    suspend fun getTaxSlabs(@Query("financialYear") financialYear: String? = null): Response<List<TaxSlab>>

    @POST("api/tax-slabs")
    suspend fun createTaxSlab(@Body request: TaxSlab): Response<TaxSlab>

    @PUT("api/tax-slabs/{id}")
    suspend fun updateTaxSlab(@Path("id") id: String, @Body request: TaxSlab): Response<TaxSlab>

    @DELETE("api/tax-slabs/{id}")
    suspend fun deleteTaxSlab(@Path("id") id: String): Response<ResponseBody>

    // --- PAY STRUCTURES ---
    @GET("api/pay-structures")
    suspend fun getPayrollStructures(): Response<List<PayStructure>>

    @POST("api/pay-structures")
    suspend fun createPayStructure(@Body request: PayStructure): Response<PayStructureResponse>

    @PUT("api/pay-structures/{id}")
    suspend fun updatePayStructure(@Path("id") id: String, @Body request: PayStructure): Response<ResponseBody>

    @DELETE("api/pay-structures/{id}")
    suspend fun deletePayStructure(@Path("id") id: String): Response<ResponseBody>

    // --- EMPLOYEE PAY STRUCTURE ---
    @POST("api/emp-pay-structures/assign")
    suspend fun assignEmpPayStructure(@Body request: EmpPayStructureAssign): Response<EmpPayStructureResponse>

    @GET("api/emp-pay-structures/{emp_id}")
    suspend fun getEmpPayStructure(@Path("emp_id") empId: String): Response<EmpPayStructureResponse>

    // --- SALARY COMPONENTS ---
    @GET("api/salary-components")
    suspend fun getSalaryComponents(): Response<List<SalaryComponentInfo>>

    @POST("api/salary-components")
    suspend fun createSalaryComponent(@Body request: SalaryComponentInfo): Response<ResponseBody>

    @PUT("api/salary-components/{id}")
    suspend fun updateSalaryComponent(@Path("id") id: String, @Body request: SalaryComponentInfo): Response<ResponseBody>

    @DELETE("api/salary-components/{id}")
    suspend fun deleteSalaryComponent(@Path("id") id: String): Response<ResponseBody>

    // --- PAYROLL RUN/PROCESS ---
    @POST("api/payroll/run")
    suspend fun runPayroll(@Body request: PayrollRunRequest): Response<PayrollRunResponse>

    @POST("api/payroll/process/{id}")
    suspend fun processPayroll(@Path("id") id: String): Response<ResponseBody>

}
