package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.*
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class PayrollRepository {

    private val pmsApi = RetrofitClient.pmsApi

    // =========================
    // ✅ PAYROLL DETAILS (NEW: Matches GET /api/salary?empId=xxx&month=4&year=2026)
    // =========================
    suspend fun getPayrollByMonthYear(
        empId: String,
        month: Int,
        year: Int
    ): Result<PayrollDetailsResponse> {
        return try {
            Log.d("PayrollRepo", "========== GET PAYROLL BY MONTH/YEAR ==========")
            Log.d("PayrollRepo", "Employee ID: $empId")
            Log.d("PayrollRepo", "Month: $month, Year: $year")

            // ✅ Calls the correct API method with query params
            val response = pmsApi.getPayrollByMonthYear(empId, month, year)

            Log.d("PayrollRepo", "Response Code: ${response.code()}")
            Log.d("PayrollRepo", "Is Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("PayrollRepo", "Response Body:")
                Log.d("PayrollRepo", "  empId: ${body?.empId}")
                Log.d("PayrollRepo", "  month: ${body?.month}")
                Log.d("PayrollRepo", "  year: ${body?.year}")
                Log.d("PayrollRepo", "  status: ${body?.status}")
                Log.d("PayrollRepo", "  grossSalary: ${body?.grossSalary}")
                Log.d("PayrollRepo", "  netSalary: ${body?.netSalary}")
                Log.d("PayrollRepo", "  totalDeductions: ${body?.totalDeductions}")
                Log.d("PayrollRepo", "  components count: ${body?.components?.size}")

                body?.components?.forEachIndexed { index, component ->
                    Log.d("PayrollRepo", "  Component ${index + 1}: ${component.compName} - ${component.amount} (${component.compType})")
                }
                Log.d("PayrollRepo", "================================================")

                Result.success(body ?: throw Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PayrollRepo", "Error Code: ${response.code()}")
                Log.e("PayrollRepo", "Error Body: $errorBody")
                Log.d("PayrollRepo", "================================================")
                Result.failure(Exception("Failed to fetch payroll: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("PayrollRepo", "========== EXCEPTION ==========")
            Log.e("PayrollRepo", "Exception: ${e.message}", e)
            Log.d("PayrollRepo", "================================================")
            Result.failure(e)
        }
    }

    // =========================
    // 🔁 LEGACY: Keep old method for backward compatibility (optional)
    // =========================
    @Deprecated("Use getPayrollByMonthYear instead")
    suspend fun getPayrollDetails(empId: String): Result<PayrollDetailsResponse> {
        return try {
            Log.d("PayrollRepo", "========== GET PAYROLL DETAILS (LEGACY) ==========")
            Log.d("PayrollRepo", "Employee ID: $empId")

            val response = pmsApi.getPayrollDetails(empId) // Old endpoint

            Log.d("PayrollRepo", "Response Code: ${response.code()}")
            Log.d("PayrollRepo", "Is Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("PayrollRepo", "Response Body: $body")
                Log.d("PayrollRepo", "================================================")
                Result.success(body ?: throw Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PayrollRepo", "Error Code: ${response.code()}")
                Log.e("PayrollRepo", "Error Body: $errorBody")
                Log.d("PayrollRepo", "================================================")
                Result.failure(Exception("Failed to fetch payroll details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PayrollRepo", "========== EXCEPTION ==========")
            Log.e("PayrollRepo", "Exception: ${e.message}", e)
            Log.d("PayrollRepo", "================================================")
            Result.failure(e)
        }
    }

    // =========================
    // 🔁 LEGACY: Old POST method (if still needed)
    // =========================
    @Deprecated("Use getPayrollByMonthYear instead")
    suspend fun getPayrollDetailsForMonthYear(
        empId: String,
        month: Int,
        year: Int
    ): Result<PayrollDetailsResponse> {
        return try {
            Log.d("PayrollRepo", "========== GET PAYROLL DETAILS (POST - LEGACY) ==========")
            Log.d("PayrollRepo", "Employee ID: $empId, Month: $month, Year: $year")

            val request = PayrollRequest(empId = empId, month = month, year = year)
            val response = pmsApi.getPayrollDetailsForMonthYear(request) // Old POST endpoint

            Log.d("PayrollRepo", "Response Code: ${response.code()}")
            Log.d("PayrollRepo", "Is Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("PayrollRepo", "Response Body: $body")
                Log.d("PayrollRepo", "================================================")
                Result.success(body ?: throw Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PayrollRepo", "Error Code: ${response.code()}")
                Log.e("PayrollRepo", "Error Body: $errorBody")
                Log.d("PayrollRepo", "================================================")
                Result.failure(Exception("Failed to fetch payroll: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PayrollRepo", "========== EXCEPTION ==========")
            Log.e("PayrollRepo", "Exception: ${e.message}", e)
            Log.d("PayrollRepo", "================================================")
            Result.failure(e)
        }
    }

    // =========================
    // PAYSLIP DOWNLOAD (Unchanged - works as-is)
    // =========================
    suspend fun downloadPayslip(empId: String, month: Int, year: Int): Result<ResponseBody> {
        return try {
            Log.d("PayrollRepo", "========== DOWNLOAD PAYSLIP PDF ==========")
            Log.d("PayrollRepo", "Employee ID: $empId, Month: $month, Year: $year")

            val response = pmsApi.downloadPayslipPdf(empId, month, year)

            Log.d("PayrollRepo", "Response Code: ${response.code()}")
            Log.d("PayrollRepo", "Is Successful: ${response.isSuccessful}")
            Log.d("PayrollRepo", "Content-Type: ${response.headers()["Content-Type"]}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val contentLength = body.contentLength()
                    Log.d("PayrollRepo", "PDF Size: $contentLength bytes")
                    Log.d("PayrollRepo", "================================================")
                    Result.success(body)
                } else {
                    Log.e("PayrollRepo", "Error: Empty response body")
                    Log.d("PayrollRepo", "================================================")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PayrollRepo", "Error Code: ${response.code()}")
                Log.e("PayrollRepo", "Error Body: $errorBody")
                Log.d("PayrollRepo", "================================================")
                Result.failure(Exception("Failed to download payslip: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PayrollRepo", "========== EXCEPTION ==========")
            Log.e("PayrollRepo", "Exception: ${e.message}", e)
            Log.d("PayrollRepo", "================================================")
            Result.failure(e)
        }
    }

    suspend fun savePayslipToFile(
        empId: String,
        month: Int,
        year: Int,
        outputFile: File
    ): Result<File> {
        return try {
            downloadPayslip(empId, month, year).onSuccess { responseBody ->
                responseBody.byteStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("PayrollRepo", "Payslip saved to: ${outputFile.absolutePath}")
            }.getOrThrow()

            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e("PayrollRepo", "Failed to save payslip: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================
    // PAYROLL STRUCTURES (Unchanged)
    // =========================
    suspend fun getPayrollStructures(): Result<List<PayrollStructure>> {
        return try {
            Log.d("PayrollRepo", "========== GET PAYROLL STRUCTURES ==========")

            val response = pmsApi.getPayrollStructures()

            Log.d("PayrollRepo", "Response Code: ${response.code()}")
            Log.d("PayrollRepo", "Is Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                Log.d("PayrollRepo", "Total Structures: ${body.size}")
                Log.d("PayrollRepo", "================================================")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PayrollRepo", "Error Code: ${response.code()}")
                Log.e("PayrollRepo", "Error Body: $errorBody")
                Log.d("PayrollRepo", "================================================")
                Result.failure(Exception("Failed to fetch payroll structures: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PayrollRepo", "========== EXCEPTION ==========")
            Log.e("PayrollRepo", "Exception: ${e.message}", e)
            Log.d("PayrollRepo", "================================================")
            Result.failure(e)
        }
    }

    // =========================
    // SALARY COMPONENTS (Unchanged)
    // =========================
    suspend fun getSalaryComponents(): Result<List<SalaryComponentInfo>> {
        return try {
            Log.d("PayrollRepo", "========== GET SALARY COMPONENTS ==========")

            val response = pmsApi.getSalaryComponents()

            Log.d("PayrollRepo", "Response Code: ${response.code()}")
            Log.d("PayrollRepo", "Is Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                Log.d("PayrollRepo", "Total Components: ${body.size}")
                Log.d("PayrollRepo", "================================================")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PayrollRepo", "Error Code: ${response.code()}")
                Log.e("PayrollRepo", "Error Body: $errorBody")
                Log.d("PayrollRepo", "================================================")
                Result.failure(Exception("Failed to fetch salary components: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PayrollRepo", "========== EXCEPTION ==========")
            Log.e("PayrollRepo", "Exception: ${e.message}", e)
            Log.d("PayrollRepo", "================================================")
            Result.failure(e)
        }
    }

    // =========================
    // UTILITY METHODS (Unchanged)
    // =========================
    fun categorizeComponents(components: List<SalaryComponent>?): Pair<List<SalaryComponent>, List<SalaryComponent>> {
        val earnings = mutableListOf<SalaryComponent>()
        val deductions = mutableListOf<SalaryComponent>()

        components?.forEach { component ->
            if (component.compType == "EARNING") {
                earnings.add(component)
            } else if (component.compType == "DEDUCTION") {
                deductions.add(component)
            }
        }

        return Pair(earnings, deductions)
    }

    fun calculateTotalEarnings(components: List<SalaryComponent>?): Double {
        return components
            ?.filter { it.compType == "EARNING" }
            ?.mapNotNull { it.amount }
            ?.sum() ?: 0.0
    }

    fun calculateTotalDeductions(components: List<SalaryComponent>?): Double {
        return components
            ?.filter { it.compType == "DEDUCTION" }
            ?.mapNotNull { it.amount }
            ?.sum() ?: 0.0
    }
}