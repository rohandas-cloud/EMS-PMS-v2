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

            try {
                // ✅ Uses CombinedRepository method that calls GET /api/salary with query params
                val result = combinedRepository.fetchPayrollByMonthYear(empId, month, year)

                result.onSuccess { body ->
                    Log.d("PayrollViewModel", "✅ Success: gross=${body.grossSalary}, net=${body.netSalary}")
                    _payrollData.value = body
                }.onFailure { error ->
                    Log.e("PayrollViewModel", "❌ Failed: ${error.message}")
                    _errorMessage.value = "Failed to fetch payroll: ${error.message}"
                }
            } catch (e: Exception) {
                Log.e("PayrollViewModel", "❌ Exception: ${e.message}", e)
                _errorMessage.value = "Network error: ${e.message}"
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

        if (empId.isNullOrBlank()) {
            _errorMessage.value = "Employee ID not found. Please login again."
            Log.e("PayrollViewModel", "❌ PMS empId is null or blank - cannot fetch payroll")
            return
        }

        Log.d("PayrollViewModel", "fetchPayrollFromSession: empId=$empId, month=$month, year=$year")
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
        fetchPayrollFromSession(month ?: 4, year ?: 2026)
    }

    // =========================
    // DOWNLOAD PAYSLIP PDF (Existing - unchanged, works as-is)
    // =========================
    fun downloadPayslip(empSalaryId: String) {
        _errorMessage.value = "Please use FilterPayslipActivity to download by month/year"
        Log.w("PayrollViewModel", "downloadPayslip by empSalaryId not supported in PMS")
    }

    fun downloadPayslipByMonth(month: Int, year: Int) {
        val empId = MyApplication.sessionManager.fetchEmpIdPms()

        if (empId == null) {
            _errorMessage.value = "Employee ID not found. Please login again."
            return
        }

        Log.d("PayrollViewModel", "downloadPayslipByMonth: empId=$empId, month=$month, year=$year")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = combinedRepository.fetchPayslipPdf(empId, month, year)

                result.onSuccess { responseBody ->
                    _downloadByMonthResult.value = Result.success(responseBody)
                }.onFailure { error ->
                    Log.e("PayrollViewModel", "Download failed: ${error.message}")
                    _downloadByMonthResult.value = Result.failure(error)
                    _errorMessage.value = "Download failed: ${error.message}"
                }
            } catch (e: Exception) {
                Log.e("PayrollViewModel", "Download exception: ${e.message}", e)
                _downloadByMonthResult.value = Result.failure(e)
                _errorMessage.value = "Download error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================
    // FETCH PAYROLL DETAIL BY empSalaryId (EMS endpoint - placeholder)
    // =========================
    fun fetchPayrollDetail(empSalaryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Implement when EMS endpoint is available
                _errorMessage.value = "Detail endpoint not yet implemented for EMS"
                Log.w("PayrollViewModel", "fetchPayrollDetail not implemented yet")
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
}