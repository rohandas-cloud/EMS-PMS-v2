package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.repository.CombinedRepository
import com.example.myapplication.data.model.*
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

/**
 * Dashboard ViewModel
 * Demonstrates how to fetch and combine data from both PMS and EMS systems
 */
class DashboardViewModel : ViewModel() {

    private val combinedRepository = CombinedRepository(
        pmsApi = RetrofitClient.pmsApi,
        emsApi = RetrofitClient.emsApi
    )

    // =========================
    // UI STATE
    // =========================
    
    // Attendance (from PMS)
    private val _attendanceData = MutableLiveData<List<AttendanceResponse>>()
    val attendanceData: LiveData<List<AttendanceResponse>> get() = _attendanceData

    // Leave Balance (from PMS)
    private val _leaveBalanceData = MutableLiveData<LeaveBalanceResponse>()
    val leaveBalanceData: LiveData<LeaveBalanceResponse> get() = _leaveBalanceData

    // Payroll Summary (from EMS)
    private val _payrollSummaryData = MutableLiveData<PayrollSummaryResponse>()
    val payrollSummaryData: LiveData<PayrollSummaryResponse> get() = _payrollSummaryData

    // Holidays (from PMS)
    private val _holidaysData = MutableLiveData<List<Holiday>>()
    val holidaysData: LiveData<List<Holiday>> get() = _holidaysData

    // Payroll Details (from PMS)
    private val _payrollDetailsData = MutableLiveData<PayrollDetailsResponse>()
    val payrollDetailsData: LiveData<PayrollDetailsResponse> get() = _payrollDetailsData

    // Payroll Structures (from PMS)
    private val _payrollStructuresData = MutableLiveData<List<PayrollStructure>>()
    val payrollStructuresData: LiveData<List<PayrollStructure>> get() = _payrollStructuresData

    // Salary Components (from PMS)
    private val _salaryComponentsData = MutableLiveData<List<SalaryComponentInfo>>()
    val salaryComponentsData: LiveData<List<SalaryComponentInfo>> get() = _salaryComponentsData

    // Loading States (separate for PMS and EMS)
    private val _isPmsLoading = MutableLiveData<Boolean>()
    val isPmsLoading: LiveData<Boolean> get() = _isPmsLoading

    private val _isEmsLoading = MutableLiveData<Boolean>()
    val isEmsLoading: LiveData<Boolean> get() = _isEmsLoading

    // Error States
    private val _pmsErrorMessage = MutableLiveData<String?>()
    val pmsErrorMessage: LiveData<String?> get() = _pmsErrorMessage

    private val _emsErrorMessage = MutableLiveData<String?>()
    val emsErrorMessage: LiveData<String?> get() = _emsErrorMessage

    // =========================
    // FETCH ALL DASHBOARD DATA
    // =========================
    fun fetchAllDashboardData() {
        viewModelScope.launch {
            _isPmsLoading.value = true
            _isEmsLoading.value = true
            _pmsErrorMessage.value = null
            _emsErrorMessage.value = null

            try {
                val result = combinedRepository.fetchDashboardData()

                // Handle PMS data
                result.attendanceData?.let { _attendanceData.value = it }
                result.leaveBalanceData?.let { _leaveBalanceData.value = it }
                result.holidaysData?.let { _holidaysData.value = it }

                // Handle EMS data - DISABLED (EMS endpoints removed except login)
                /*
                result.payrollSummaryData?.let { _payrollSummaryData.value = it }
                */

                // Handle errors
                if (result.hasErrors) {
                    val errorMessages = result.errorMessages
                    Log.w("DashboardVM", "Some data fetch failed: $errorMessages")
                    
                    // Categorize errors
                    errorMessages.forEach { errorMsg ->
                        if (errorMsg.startsWith("Attendance:") || 
                            errorMsg.startsWith("Leave Balance:") || 
                            errorMsg.startsWith("Holidays:")) {
                            _pmsErrorMessage.value = errorMsg
                        } else if (errorMsg.startsWith("Payroll:")) {
                            _emsErrorMessage.value = errorMsg
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardVM", "Failed to fetch dashboard data", e)
                _pmsErrorMessage.value = "Failed to load PMS data"
                _emsErrorMessage.value = "Failed to load EMS data"
            } finally {
                _isPmsLoading.value = false
                _isEmsLoading.value = false
            }
        }
    }

    // =========================
    // INDIVIDUAL DATA FETCHING
    // =========================

    fun fetchAttendanceHistory() {
        viewModelScope.launch {
            _isPmsLoading.value = true
            val empId = MyApplication.sessionManager.fetchEmpIdPms()
            
            if (empId == null) {
                _pmsErrorMessage.value = "Employee ID not found"
                _isPmsLoading.value = false
                return@launch
            }

            val result = combinedRepository.fetchPmsAttendanceHistory(empId)
            result.onSuccess { _attendanceData.value = it }
                .onFailure { _pmsErrorMessage.value = it.message }
            
            _isPmsLoading.value = false
        }
    }

    fun fetchLeaveBalance() {
        viewModelScope.launch {
            _isPmsLoading.value = true
            val empId = MyApplication.sessionManager.fetchEmpIdPms()
            
            if (empId == null) {
                _pmsErrorMessage.value = "Employee ID not found"
                _isPmsLoading.value = false
                return@launch
            }

            val result: Result<LeaveBalanceResponse> = combinedRepository.fetchPmsLeaveBalance(empId)
            result.onSuccess { _leaveBalanceData.value = it }
                .onFailure { _pmsErrorMessage.value = it.message }
            
            _isPmsLoading.value = false
        }
    }

    // NOTE: EMS endpoints (except login) have been removed from EmsApiService
    /*
    fun fetchPayrollSummary(month: Int? = null, year: Int? = null) {
        viewModelScope.launch {
            _isEmsLoading.value = true
            
            val result = combinedRepository.fetchEmsPayrollSummary(month, year)
            result.onSuccess { _payrollSummaryData.value = it }
                .onFailure { _emsErrorMessage.value = it.message }
            
            _isEmsLoading.value = false
        }
    }
    */

    fun fetchHolidays() {
        viewModelScope.launch {
            _isPmsLoading.value = true
            
            val result: Result<List<Holiday>> = combinedRepository.fetchPmsHolidays()
            result.onSuccess { _holidaysData.value = it }
                .onFailure { _pmsErrorMessage.value = it.message }
            
            _isPmsLoading.value = false
        }
    }

    fun fetchPayrollDetails() {
        viewModelScope.launch {
            _isPmsLoading.value = true
            val empId = MyApplication.sessionManager.fetchEmpIdPms()

            if (empId == null) {
                _pmsErrorMessage.value = "Employee ID not found"
                _isPmsLoading.value = false
                return@launch
            }

            val result: Result<PayrollDetailsResponse> = combinedRepository.fetchPmsPayrollDetails(empId)
            result.onSuccess { _payrollDetailsData.value = it }
                .onFailure { _pmsErrorMessage.value = it.message }

            _isPmsLoading.value = false
        }
    }

    fun fetchPayrollDetailsForMonthYear(month: Int, year: Int) {
        viewModelScope.launch {
            _isPmsLoading.value = true
            val empId = MyApplication.sessionManager.fetchEmpIdPms()

            if (empId == null) {
                _pmsErrorMessage.value = "Employee ID not found"
                _isPmsLoading.value = false
                return@launch
            }

            val result: Result<PayrollDetailsResponse> =
                combinedRepository.fetchPmsPayrollDetailsForMonthYear(empId, month, year)
            result.onSuccess { _payrollDetailsData.value = it }
                .onFailure { _pmsErrorMessage.value = it.message }

            _isPmsLoading.value = false
        }
    }

    fun fetchPayrollStructures() {
        viewModelScope.launch {
            _isPmsLoading.value = true

            val result: Result<List<PayrollStructure>> = combinedRepository.fetchPayrollStructures()
            result.onSuccess { _payrollStructuresData.value = it }
                .onFailure { _pmsErrorMessage.value = it.message }

            _isPmsLoading.value = false
        }
    }

    fun fetchSalaryComponents() {
        viewModelScope.launch {
            _isPmsLoading.value = true

            val result: Result<List<SalaryComponentInfo>> = combinedRepository.fetchSalaryComponents()
            result.onSuccess { _salaryComponentsData.value = it }
                .onFailure { _pmsErrorMessage.value = it.message }

            _isPmsLoading.value = false
        }
    }

    fun downloadPayslip(empId: String, month: Int, year: Int): LiveData<Result<ResponseBody>> {
        val resultLiveData = MutableLiveData<Result<ResponseBody>>()
        viewModelScope.launch {
            _isPmsLoading.value = true

            val result: Result<ResponseBody> = combinedRepository.fetchPayslipPdf(empId, month, year)
            resultLiveData.value = result
            result.onFailure { _pmsErrorMessage.value = it.message }

            _isPmsLoading.value = false
        }
        return resultLiveData
    }
}
