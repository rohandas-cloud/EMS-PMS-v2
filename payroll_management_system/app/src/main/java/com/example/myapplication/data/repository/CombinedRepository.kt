package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.PmsApiService
import com.example.myapplication.data.api.EmsApiService
import com.example.myapplication.data.model.*
import okhttp3.ResponseBody

/**
 * Data class for combined dashboard data.
 * Moved to top-level for better visibility and structure.
 */
data class DashboardDataResult(
    val attendanceResult: Result<List<AttendanceResponse>>?,
    val leaveBalanceResult: Result<LeaveBalanceResponse>?,
    val payrollSummaryResult: Result<PayrollSummaryResponse>?,
    val holidaysResult: Result<List<Holiday>>?
) {
    val attendanceData: List<AttendanceResponse>?
        get() = attendanceResult?.getOrNull()

    val leaveBalanceData: LeaveBalanceResponse?
        get() = leaveBalanceResult?.getOrNull()

    val holidaysData: List<Holiday>?
        get() = holidaysResult?.getOrNull()

    /**
     * Checks if any of the attempted fetch operations resulted in a failure.
     */
    val hasErrors: Boolean
        get() = listOfNotNull(
            attendanceResult,
            leaveBalanceResult,
            payrollSummaryResult,
            holidaysResult
        ).any { it.isFailure }

    /**
     * Collects error messages from all failed results.
     */
    val errorMessages: List<String>
        get() = listOfNotNull(
            attendanceResult?.exceptionOrNull()?.message?.let { "Attendance: $it" },
            leaveBalanceResult?.exceptionOrNull()?.message?.let { "Leave Balance: $it" },
            payrollSummaryResult?.exceptionOrNull()?.message?.let { "Payroll: $it" },
            holidaysResult?.exceptionOrNull()?.message?.let { "Holidays: $it" }
        )
}

/**
 * Combined Repository
 * Coordinates data fetching from the PMS system.
 */
class CombinedRepository(
    private val pmsApi: PmsApiService,
    private val emsApi: EmsApiService? = null
) {

    // =========================
    // PMS DATA FETCHING
    // =========================

    suspend fun fetchPmsAttendanceHistory(empId: String): Result<List<AttendanceResponse>> {
        return try {
            val response = pmsApi.getAttendanceHistory(empId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("PMS Attendance fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "PMS Attendance error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchPmsLeaveBalance(empId: String): Result<LeaveBalanceResponse> {
        return try {
            val response = pmsApi.getLeaveBalance(empId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("PMS Leave balance empty response"))
            } else {
                Result.failure(Exception("PMS Leave balance failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "PMS Leave balance error", e)
            Result.failure(e)
        }
    }

    @Suppress("unused")
    suspend fun fetchPmsLeaveHistory(): Result<List<LeaveResponse>> {
        return try {
            val response = pmsApi.getLeaveHistory()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("PMS Leave history failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "PMS Leave history error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchPmsHolidays(): Result<List<Holiday>> {
        return try {
            val response = pmsApi.getHolidays()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("PMS Holidays fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "PMS Holidays error", e)
            Result.failure(e)
        }
    }

    // ====================
    // PAYROLL DATA FETCHING
    // ====================

    suspend fun fetchPmsPayrollDetails(empId: String): Result<PayrollDetailsResponse> {
        return try {
            val response = pmsApi.getPayrollDetails(empId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("PMS Payroll details empty response"))
            } else {
                Result.failure(Exception("PMS Payroll details failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "PMS Payroll details error", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch Payroll by empId + month + year (GET /api/salary?empId=xxx&month=4&year=2026)
     * Tries EMS API first (with EMS token), falls back to PMS API
     */
    suspend fun fetchPayrollByMonthYear(
        empId: String,
        month: Int,
        year: Int
    ): Result<PayrollDetailsResponse> {
        return try {
            // Debug: Log which token is being used
            val pmsToken = MyApplication.sessionManager.fetchPmsToken()
            val emsToken = MyApplication.sessionManager.fetchEmsToken()
            Log.d("CombinedRepo", "PMS Token exists: ${pmsToken != null}, length: ${pmsToken?.length ?: 0}")
            Log.d("CombinedRepo", "EMS Token exists: ${emsToken != null}, length: ${emsToken?.length ?: 0}")
            
            // Try EMS API first if available (payroll typically comes from EMS)
            if (emsApi != null) {
                Log.d("CombinedRepo", "Attempting to fetch payroll from EMS API...")
                return fetchPayrollFromEms(empId, month, year)
            }
            
            // Fallback to PMS API
            Log.d("CombinedRepo", "Fetching payroll from PMS API...")
            fetchPayrollFromPms(empId, month, year)
        } catch (e: Exception) {
            Log.e("CombinedRepo", "fetchPayrollByMonthYear exception", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetch payroll from EMS API (uses EMS token)
     */
    private suspend fun fetchPayrollFromEms(
        empId: String,
        month: Int,
        year: Int
    ): Result<PayrollDetailsResponse> {
        return try {
            // Validate empId before making request
            if (empId.isBlank()) {
                Log.e("CombinedRepo", "❌ EMS empId is blank - cannot fetch payroll")
                return Result.failure(Exception("Invalid Employee ID"))
            }

            val sessionEmpId = MyApplication.sessionManager.fetchEmpIdEms()
            if (sessionEmpId != null && sessionEmpId != empId) {
                Log.w("CombinedRepo", "⚠️ empId mismatch: provided=$empId, session=$sessionEmpId")
            }

            Log.d("CombinedRepo", "Making EMS API call with empId: $empId, month: $month, year: $year")
            val response = emsApi!!.getPayrollByMonthYear(empId, month, year)

            // Check if response is HTML instead of JSON
            val contentType = response.headers()["Content-Type"] ?: ""
            if (contentType.contains("text/html", ignoreCase = true)) {
                Log.w("CombinedRepo", "EMS API returned HTML, falling back to PMS...")
                return fetchPayrollFromPms(empId, month, year)
            }

            if (response.isSuccessful) {
                Log.d("CombinedRepo", "✅ EMS payroll fetch successful")
                response.body()?.let { body ->
                    // Validate response empId matches request
                    if (body.empId != null && body.empId != empId) {
                        Log.w("CombinedRepo", "⚠️ MISMATCH: Request empId ($empId) != Response empId (${body.empId})")
                    } else {
                        Log.d("CombinedRepo", "✅ empId validated: $empId")
                    }
                    Result.success(body)
                } ?: Result.failure(Exception("Empty response body from EMS server"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.w("CombinedRepo", "EMS API failed: ${response.code()}, falling back to PMS...")
                
                // Fallback to PMS API
                fetchPayrollFromPms(empId, month, year)
            }
        } catch (e: Exception) {
            Log.w("CombinedRepo", "EMS API exception, falling back to PMS: ${e.message}")
            fetchPayrollFromPms(empId, month, year)
        }
    }
    
    /**
     * Fetch payroll from PMS API (uses PMS token)
     */
    private suspend fun fetchPayrollFromPms(
        empId: String,
        month: Int,
        year: Int
    ): Result<PayrollDetailsResponse> {
        return try {
            val response = pmsApi.getPayrollByMonthYear(empId, month, year)

            // Check if response is HTML instead of JSON
            val contentType = response.headers()["Content-Type"] ?: ""
            if (contentType.contains("text/html", ignoreCase = true)) {
                return Result.failure(Exception("API returned HTML instead of JSON. Verify endpoint configuration."))
            }

            if (response.isSuccessful) {
                Log.d("CombinedRepo", "✅ PMS payroll fetch successful")
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response body from PMS server"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("CombinedRepo", "PMS API Error ${response.code()}: $errorBody")
                
                // Check if it's a 401 - token issue
                if (response.code() == 401) {
                    val pmsToken = MyApplication.sessionManager.fetchPmsToken()
                    Log.e("CombinedRepo", "401 Unauthorized - PMS Token may be expired or invalid")
                    Log.e("CombinedRepo", "PMS Token (first 30 chars): ${pmsToken?.take(30)}")
                }
                
                Result.failure(Exception("Failed to fetch payroll: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "PMS API exception", e)
            Result.failure(e)
        }
    }

    @Deprecated("Use fetchPayrollByMonthYear instead", ReplaceWith("fetchPayrollByMonthYear(empId, month, year)"))
    suspend fun fetchPmsPayrollDetailsForMonthYear(
        empId: String,
        month: Int,
        year: Int
    ): Result<PayrollDetailsResponse> {
        return try {
            val request = PayrollRequest(empId = empId, month = month, year = year)
            val response = pmsApi.getPayrollDetailsForMonthYear(request)

            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to fetch payroll (legacy): ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPayslipPdf(empId: String, month: Int, year: Int): Result<ResponseBody> {
        return try {
            val response = pmsApi.downloadPayslipPdf(empId, month, year)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Payslip PDF empty response"))
            } else {
                Result.failure(Exception("Payslip PDF failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "Payslip PDF error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchPayrollStructures(): Result<List<PayrollStructure>> {
        return try {
            val response = pmsApi.getPayrollStructures()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Payroll structures fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "Payroll structures error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchSalaryComponents(): Result<List<SalaryComponentInfo>> {
        return try {
            val response = pmsApi.getSalaryComponents()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Salary components fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "Salary components error", e)
            Result.failure(e)
        }
    }

    // =========================
    // COMBINED OPERATIONS
    // =========================

    /**
     * Fetches all data required for the Dashboard in parallel (conceptually, though currently sequential).
     */
    suspend fun fetchDashboardData(): DashboardDataResult {
        val empIdPms = MyApplication.sessionManager.fetchEmpIdPms()

        var attendanceResult: Result<List<AttendanceResponse>>? = null
        var leaveBalanceResult: Result<LeaveBalanceResponse>? = null
        var holidaysResult: Result<List<Holiday>>? = null
        val payrollSummaryResult: Result<PayrollSummaryResponse>? = null // Placeholder for future implementation

        if (empIdPms != null) {
            attendanceResult = fetchPmsAttendanceHistory(empIdPms)
            leaveBalanceResult = fetchPmsLeaveBalance(empIdPms)
            holidaysResult = fetchPmsHolidays()
        }

        return DashboardDataResult(
            attendanceResult = attendanceResult,
            leaveBalanceResult = leaveBalanceResult,
            payrollSummaryResult = payrollSummaryResult,
            holidaysResult = holidaysResult
        )
    }
}
