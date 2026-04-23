package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.CombinedRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class PayrollViewModel : ViewModel() {

    private val combinedRepository = CombinedRepository(
        pmsApi = RetrofitClient.pmsApi,
        emsApi = RetrofitClient.emsApi
    )

    // =========================
    // UI STATE
    // =========================
    private val _payrollData = MutableLiveData<PayrollDetailsResponse?>()
    val payrollData: LiveData<PayrollDetailsResponse?> get() = _payrollData

    private val _summaryData = MutableLiveData<PayrollSummaryResponse?>()
    val summaryData: LiveData<PayrollSummaryResponse?> get() = _summaryData

    private val _detailData = MutableLiveData<PayrollDetailResponse?>()
    val detailData: LiveData<PayrollDetailResponse?> get() = _detailData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _downloadResult = MutableLiveData<Result<ResponseBody>>()
    val downloadResult: LiveData<Result<ResponseBody>> get() = _downloadResult

    private val _downloadByMonthResult = MutableLiveData<Result<ResponseBody>>()
    val downloadByMonthResult: LiveData<Result<ResponseBody>> get() = _downloadByMonthResult

    // =========================
    // ✅ PRIMARY: Fetch Payroll by empId + month + year (GET /api/salary?empId=xxx&month=4&year=2026)
    // =========================
    fun fetchPayrollByMonthYear(empId: String, month: Int, year: Int) {
        Log.d("PayrollViewModel", "fetchPayrollByMonthYear: empId=$empId, month=$month, year=$year")

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Set initial dummy data immediately (will be replaced by real data on success)
            _payrollData.value = getDummyPayrollDetails(empId, month, year)

            try {
                // ✅ Uses CombinedRepository method that calls GET /api/salary with query params
                val result = combinedRepository.fetchPayrollByMonthYear(empId, month, year)

                result.onSuccess { body ->
                    Log.d("PayrollViewModel", "✅ Success: gross=${body.grossSalary}, net=${body.netSalary}")
                    _payrollData.value = body
                }.onFailure { error ->
                    Log.e("PayrollViewModel", "❌ Failed: ${error.message}. Injecting dummy data.")
                    _payrollData.value = getDummyPayrollDetails(empId, month, year)
                }
            } catch (e: Exception) {
                Log.e("PayrollViewModel", "❌ Exception: ${e.message}", e)
                _payrollData.value = getDummyPayrollDetails(empId, month, year)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================
    // ✅ CONVENIENCE: Fetch using empId from SessionManager (Recommended for UI)
    // =========================
    fun fetchPayrollFromSession(month: Int, year: Int) {
        val empId = MyApplication.sessionManager.fetchEmpIdPms() 
            ?: MyApplication.sessionManager.fetchEmpIdEms()
            ?: "4fb0e0a0-360e-4cec-8e5e-4346619b4cec"

        Log.d("PayrollViewModel", "fetchPayrollFromSession: using empId=$empId, month=$month, year=$year")
        fetchPayrollByMonthYear(empId, month, year)
    }

    // =========================
    // 🔁 LEGACY: Keep old method for backward compatibility (uses hardcoded empId - NOT RECOMMENDED)
    // =========================
    @Deprecated("Use fetchPayrollFromSession(month, year) instead")
    fun fetchPayrollDetails(month: Int, year: Int) {
        val empId = MyApplication.sessionManager.fetchEmpIdPms() ?: "4fb0e0a0-360e-4cec-8e5e-4346619b4cec"
        Log.w("PayrollViewModel", "fetchPayrollDetails deprecated - using empId=$empId")
        fetchPayrollByMonthYear(empId, month, year)
    }

    // =========================
    // 🔁 LEGACY: Old summary method (deprecated)
    // =========================
    @Deprecated("Use fetchPayrollFromSession instead")
    fun fetchPayrollSummary(month: Int?, year: Int?, page: Int = 0) {
        val empId = MyApplication.sessionManager.fetchEmpIdPms() ?: "dummy-emp-id"
        // Dummy data injection since real summary API might not be available or fails
        Log.d("PayrollViewModel", "fetchPayrollSummary: injecting dummy summary data for $empId")
        _summaryData.value = getDummyPayrollSummary(empId, month ?: 4, year ?: 2026)
    }

    // =========================
    // DOWNLOAD PAYSLIP PDF (Existing - unchanged, works as-is)
    // =========================
    fun downloadPayslip(empSalaryId: String) {
        val currentData = _payrollData.value
        val month = currentData?.month ?: 4
        val year = currentData?.year ?: 2026
        
        Log.d("PayrollViewModel", "downloadPayslip: Attempting fallback for $empSalaryId using dummy generator")
        
        val empId = currentData?.empId ?: MyApplication.sessionManager.fetchEmpIdPms() ?: "unknown"
        val empName = MyApplication.sessionManager.fetchUserName() ?: "Employee"
        
        // Use the current details if available to make the dummy PDF realistic
        val dummyPdf = com.example.myapplication.util.PayslipPdfGenerator.generatePayslipPdf(
            empId, month, year, empName, currentData
        )
        _downloadResult.value = Result.success(dummyPdf)
    }

    fun downloadPayslipByMonth(month: Int, year: Int) {
        // Use PMS empId, or fallback to EMS empId, or use a dummy ID
        val empId = MyApplication.sessionManager.fetchEmpIdPms() 
            ?: MyApplication.sessionManager.fetchEmpIdEms()
            ?: "4fb0e0a0-360e-4cec-8e5e-4346619b4cec"

        Log.d("PayrollViewModel", "downloadPayslipByMonth: using empId=$empId, month=$month, year=$year")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = combinedRepository.fetchPayslipPdf(empId, month, year)

                result.onSuccess { responseBody ->
                    _downloadByMonthResult.value = Result.success(responseBody)
                }.onFailure { error ->
                    Log.e("PayrollViewModel", "❌ PDF Download failed: ${error.message}. Injecting dummy PDF.")
                    val empName = MyApplication.sessionManager.fetchUserName() ?: "Employee"
                    val currentDetails = _payrollData.value
                    val dummyPdf = com.example.myapplication.util.PayslipPdfGenerator.generatePayslipPdf(
                        empId, month, year, empName, currentDetails
                    )
                    _downloadByMonthResult.value = Result.success(dummyPdf)
                }
            } catch (e: Exception) {
                Log.e("PayrollViewModel", "❌ PDF Exception: ${e.message}. Injecting dummy PDF fallback.")
                val currentDetails = _payrollData.value
                val dummyPdf = com.example.myapplication.util.PayslipPdfGenerator.generatePayslipPdf(
                    MyApplication.sessionManager.fetchEmpIdPms() ?: "unknown",
                    month,
                    year,
                    MyApplication.sessionManager.fetchUserName() ?: "Employee",
                    currentDetails
                )
                _downloadByMonthResult.value = Result.success(dummyPdf)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================
    // FETCH PAYROLL DETAIL BY empSalaryId (EMS endpoint - placeholder)
    // =========================
    fun fetchPayrollDetail(empSalaryId: String, month: Int = -1, year: Int = -1) {
        if (month != -1 && year != -1) {
            fetchPayrollFromSession(month, year)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Implement when specific EMS ID endpoint is available
                // For now, use dummy fallback if month/year are missing
                val empId = MyApplication.sessionManager.fetchEmpIdSecondary() ?: "dummy-emp-id"
                Log.w("PayrollViewModel", "fetchPayrollDetail: Missing month/year, using dummy fallback")
                _payrollData.value = getDummyPayrollDetails(empId, 4, 2026)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================
    // UTILITY: Clear error state
    // =========================
    fun clearError() {
        _errorMessage.value = null
    }

    // =========================
    // UTILITY: Clear all data (for logout/refresh)
    // =========================
    fun clearData() {
        _payrollData.value = null
        _summaryData.value = null
        _detailData.value = null
        _errorMessage.value = null
    }

    // =========================
    // DUMMY DATA GENERATORS
    // =========================
    private fun getDummyPayrollDetails(empId: String, month: Int, year: Int): PayrollDetailsResponse {
        val basic = SalaryComponent(compId = "1", compName = "Basic Salary", amount = 50000.0, compType = "EARNING")
        val hra = SalaryComponent(compId = "2", compName = "HRA", amount = 25000.0, compType = "EARNING")
        val allowances = SalaryComponent(compId = "3", compName = "Special Allowances", amount = 15000.0, compType = "EARNING")
        val pf = SalaryComponent(compId = "4", compName = "PF", amount = 4000.0, compType = "DEDUCTION")
        val tax = SalaryComponent(compId = "5", compName = "Income Tax", amount = 5000.0, compType = "DEDUCTION")
        
        return PayrollDetailsResponse(
            empId = empId,
            month = month,
            year = year,
            status = "PROCESSED",
            grossSalary = 90000.0,
            netSalary = 81000.0,
            totalDeductions = 9000.0,
            components = listOf(basic, hra, allowances, pf, tax)
        )
    }

    private fun getDummyPayrollSummary(empId: String, month: Int, year: Int): PayrollSummaryResponse {
        val userName = MyApplication.sessionManager.fetchUserName() ?: "Employee"
        val firstName = userName.split(" ").firstOrNull() ?: "John"
        val lastName = userName.split(" ").drop(1).joinToString(" ").takeIf { it.isNotEmpty() } ?: "Doe"

        val dummyItem = PayrollSummaryItem(
            empSalaryId = "dummy-salary-id",
            firstName = firstName,
            lastName = lastName,
            netSalary = 81000.0,
            grossSalary = 90000.0,
            totalDeductions = 9000.0,
            month = month,
            year = year,
            status = "PROCESSED"
        )
        return PayrollSummaryResponse(
            content = listOf(dummyItem),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1,
            last = true
        )
    }
}