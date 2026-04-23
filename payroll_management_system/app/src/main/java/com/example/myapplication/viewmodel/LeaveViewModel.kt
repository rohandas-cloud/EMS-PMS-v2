package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.*
import kotlinx.coroutines.launch

class LeaveViewModel : ViewModel() {

    private val emsApi = RetrofitClient.emsApi

    // =====================
    // LIVE DATA
    // =====================
    private val _leaveBalances = MutableLiveData<LeaveBalanceResponse?>()
    val leaveBalances: LiveData<LeaveBalanceResponse?> = _leaveBalances

    private val _leaveTypes = MutableLiveData<List<LeaveType>>()
    val leaveTypes: LiveData<List<LeaveType>> = _leaveTypes

    private val _applyResult = MutableLiveData<Pair<Boolean, String>>()
    val applyResult: LiveData<Pair<Boolean, String>> = _applyResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _leaveHistory = MutableLiveData<List<LeaveResponse>>()
    val leaveHistory: LiveData<List<LeaveResponse>> = _leaveHistory

    private val _leaveRequests = MutableLiveData<List<LeaveResponse>>()
    val leaveRequests: LiveData<List<LeaveResponse>> = _leaveRequests

    private val _leaveDetails = MutableLiveData<LeaveResponse?>()
    val leaveDetails: LiveData<LeaveResponse?> = _leaveDetails

    // =====================
    // BALANCE
    // =====================
    fun fetchLeaveBalance() {
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        
        Log.d("LeaveVM", "========== FETCH LEAVE BALANCE START ==========")
        Log.d("LeaveVM", "empId from session: $empId")
        
        if (empId == null) {
            _errorMessage.value = "Leave feature unavailable: EMS login failed. Please login again."
            Log.w("LeaveVM", "EMS empId is null - Leave feature disabled. User may have partial login (PMS only).")
            Log.d("LeaveVM", "================================================")
            return
        }

        // Validate empId is not blank
        if (empId.isBlank()) {
            _errorMessage.value = "Invalid Employee ID. Please login again."
            Log.e("LeaveVM", "EMS empId is blank - invalid session state")
            Log.d("LeaveVM", "================================================")
            return
        }

        Log.d("LeaveVM", "✅ Validated empId: $empId")

        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("LeaveVM", "Making API call with empId: $empId")
                Log.d("LeaveVM", "API URL: ${RetrofitClient.emsApi}")
                
                val response = emsApi.getLeaveBalance(empId)
                Log.d("LeaveVM", "Response Code: ${response.code()}")
                Log.d("LeaveVM", "Is Successful: ${response.isSuccessful}")
                Log.d("LeaveVM", "Response Message: ${response.message()}")
                Log.d("LeaveVM", "Response Headers: ${response.headers()}")
                Log.d("LeaveVM", "Response Body: ${response.body()}")
                Log.d("LeaveVM", "Error Body: ${response.errorBody()?.string()}")
                
                // Verify response empId matches request empId
                if (response.isSuccessful && response.body() != null) {
                    val dataList = response.body()!!
                    Log.d("LeaveVM", "Success! Leave Balance:")
                    Log.d("LeaveVM", "Full response body: $dataList")
                    Log.d("LeaveVM", "Received ${dataList.size} balance record(s)")
                    
                    // Use the first record if available
                    val data = dataList.firstOrNull()
                    
                    if (data != null) {
                        Log.d("LeaveVM", "  Request empId: $empId")
                        Log.d("LeaveVM", "  Response empId: ${data.empId}")
                        
                        // Validate response empId matches request
                        if (data.empId != null && data.empId != empId) {
                            Log.w("LeaveVM", "⚠️ MISMATCH: Request empId ($empId) != Response empId (${data.empId})")
                        }
                        
                        Log.d("LeaveVM", "  casualLeave: ${data.casualLeave}")
                        Log.d("LeaveVM", "  sickLeave: ${data.sickLeave}")
                        Log.d("LeaveVM", "  earnedLeave: ${data.earnedLeave}")
                        Log.d("LeaveVM", "  totalLeave: ${data.totalLeave}")
                        Log.d("LeaveVM", "  message: ${data.message}")
                        Log.d("LeaveVM", "  Raw object: $data")
                        _leaveBalances.value = data
                    } else {
                        Log.w("LeaveVM", "Empty balance list received")
                        _leaveBalances.value = null
                    }
                    Log.d("LeaveVM", "================================================")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LeaveVM", "Failed to fetch balance. Code: ${response.code()}")
                    Log.e("LeaveVM", "Error Message: ${response.message()}")
                    Log.e("LeaveVM", "Error Body: $errorBody")
                    Log.e("LeaveVM", "Response Headers: ${response.headers()}")
                    _error.value = "Failed to load balance: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "========== FETCH LEAVE BALANCE FAILED ==========")
                Log.e("LeaveVM", "Exception: ${e.message}")
                Log.e("LeaveVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("LeaveVM", "================================================")
                _error.value = e.message ?: "Failed to load balance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =====================
    // TYPES
    // =====================
    fun fetchLeaveTypes() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("LeaveVM", "========== FETCH LEAVE TYPES ==========")
                
                val response = emsApi.getLeaveTypes()
                Log.d("LeaveVM", "Response Code: ${response.code()}")
                Log.d("LeaveVM", "Is Successful: ${response.isSuccessful}")
                Log.d("LeaveVM", "Response Message: ${response.message()}")
                Log.d("LeaveVM", "Response Headers: ${response.headers()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("LeaveVM", "Success! Received ${data.size} leave types")
                    Log.d("LeaveVM", "Full response body: $data")
                    data.forEachIndexed { index, leaveType ->
                        Log.d("LeaveVM", "  Type ${index + 1}:")
                        Log.d("LeaveVM", "    typeId: ${leaveType.typeId}")
                        Log.d("LeaveVM", "    type: ${leaveType.type}")
                        Log.d("LeaveVM", "    carryForwardAllowed: ${leaveType.carryForwardAllowed}")
                        Log.d("LeaveVM", "    maxConsecutiveDays: ${leaveType.maxConsecutiveDays}")
                        Log.d("LeaveVM", "    Raw object: $leaveType")
                    }
                    Log.d("LeaveVM", "================================================")
                    _leaveTypes.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LeaveVM", "Failed to fetch leave types. Code: ${response.code()}")
                    Log.e("LeaveVM", "Error Message: ${response.message()}")
                    Log.e("LeaveVM", "Error Body: $errorBody")
                    Log.e("LeaveVM", "Response Headers: ${response.headers()}")
                    _error.value = "Failed to load leave types: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "========== FETCH LEAVE TYPES FAILED ==========")
                Log.e("LeaveVM", "Exception: ${e.message}")
                Log.e("LeaveVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("LeaveVM", "================================================")
                _error.value = e.message ?: "Failed to load leave types"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =====================
    // APPLY LEAVE
    // =====================
    fun applyLeave(request: LeaveApplyRequest) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("LeaveVM", "========== APPLY LEAVE ==========")
                Log.d("LeaveVM", "Request:")
                Log.d("LeaveVM", "  empLeaveId: ${request.empId}")
                Log.d("LeaveVM", "  leaveDay: ${request.leaveDay}")
                Log.d("LeaveVM", "  description: ${request.reason}")
                Log.d("LeaveVM", "  noOfDays: ${request.noOfDays}")
                Log.d("LeaveVM", "  startDate: ${request.startDate}")
                Log.d("LeaveVM", "  endDate: ${request.endDate}")
                Log.d("LeaveVM", "  Full request object: $request")
                
                val response = emsApi.applyLeave(request)
                Log.d("LeaveVM", "Response Code: ${response.code()}")
                Log.d("LeaveVM", "Is Successful: ${response.isSuccessful}")
                Log.d("LeaveVM", "Response Message: ${response.message()}")
                Log.d("LeaveVM", "Response Headers: ${response.headers()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("LeaveVM", "Success!")
                    Log.d("LeaveVM", "Full response body: $data")
                    Log.d("LeaveVM", "  leaveApplicationId: ${data.leaveApplicationId}")
                    Log.d("LeaveVM", "  status: ${data.status}")
                    Log.d("LeaveVM", "  message: ${data.message}")
                    Log.d("LeaveVM", "  Raw object: $data")
                    Log.d("LeaveVM", "================================================")
                    _applyResult.value = true to (data.message ?: "Leave applied successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LeaveVM", "Failed to apply leave. Code: ${response.code()}")
                    Log.e("LeaveVM", "Error Message: ${response.message()}")
                    Log.e("LeaveVM", "Error Body: $errorBody")
                    Log.e("LeaveVM", "Response Headers: ${response.headers()}")
                    Log.d("LeaveVM", "================================================")
                    _applyResult.value = false to ("Failed to apply leave: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "========== APPLY LEAVE FAILED ==========")
                Log.e("LeaveVM", "Exception: ${e.message}")
                Log.e("LeaveVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("LeaveVM", "================================================")
                _applyResult.value = false to (e.message ?: "Apply leave failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =====================
    // LEAVE HISTORY
    // =====================
    fun fetchLeaveHistory() {
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        
        Log.d("LeaveVM", "========== FETCH LEAVE HISTORY START ==========")
        Log.d("LeaveVM", "empId from session: $empId")
        
        if (empId == null) {
            _errorMessage.value = "Leave history unavailable: EMS login failed."
            Log.w("LeaveVM", "EMS empId is null - Leave history disabled.")
            Log.d("LeaveVM", "================================================")
            return
        }

        // Validate empId is not blank
        if (empId.isBlank()) {
            _errorMessage.value = "Invalid Employee ID. Please login again."
            Log.e("LeaveVM", "EMS empId is blank - invalid session state")
            Log.d("LeaveVM", "================================================")
            return
        }

        Log.d("LeaveVM", "✅ Validated empId: $empId")

        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("LeaveVM", "Making API call with empId: $empId")
                Log.d("LeaveVM", "API URL: ${RetrofitClient.emsApi}")
                
                val response = emsApi.getLeaveHistory(empId)
                Log.d("LeaveVM", "Response Code: ${response.code()}")
                Log.d("LeaveVM", "Is Successful: ${response.isSuccessful}")
                Log.d("LeaveVM", "Response Message: ${response.message()}")
                Log.d("LeaveVM", "Response Headers: ${response.headers()}")
                Log.d("LeaveVM", "Response Body: ${response.body()}")
                Log.d("LeaveVM", "Error Body: ${response.errorBody()?.string()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("LeaveVM", "Success! Received ${data.size} leave records")
                    Log.d("LeaveVM", "Full response body: $data")
                    data.forEachIndexed { index, leave ->
                        Log.d("LeaveVM", "  Leave ${index + 1}:")
                        Log.d("LeaveVM", "    Request empId: $empId")
                        Log.d("LeaveVM", "    Response empId: ${leave.empId}")
                        
                        // Validate response empId matches request
                        if (leave.empId != null && leave.empId != empId) {
                            Log.w("LeaveVM", "⚠️ MISMATCH: Request empId ($empId) != Response empId (${leave.empId})")
                        }
                        
                        Log.d("LeaveVM", "    leaveApplicationId: ${leave.leaveApplicationId}")
                        Log.d("LeaveVM", "    leaveType: ${leave.leaveType}")
                        Log.d("LeaveVM", "    startDate: ${leave.startDate}")
                        Log.d("LeaveVM", "    endDate: ${leave.endDate}")
                        Log.d("LeaveVM", "    noOfDays: ${leave.noOfDays}")
                        Log.d("LeaveVM", "    status: ${leave.status}")
                        Log.d("LeaveVM", "    Raw object: $leave")
                    }
                    Log.d("LeaveVM", "================================================")
                    _leaveHistory.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LeaveVM", "Failed to fetch history. Code: ${response.code()}")
                    Log.e("LeaveVM", "Error Message: ${response.message()}")
                    Log.e("LeaveVM", "Error Body: $errorBody")
                    Log.e("LeaveVM", "Response Headers: ${response.headers()}")
                    _errorMessage.value = "Failed to load leave history: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "========== FETCH LEAVE HISTORY FAILED ==========")
                Log.e("LeaveVM", "Exception: ${e.message}")
                Log.e("LeaveVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("LeaveVM", "================================================")
                _errorMessage.value = e.message ?: "Failed to load leave history"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =====================
    // LEAVE REQUESTS (HR)
    // =====================
    fun fetchLeaveRequests(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("LeaveVM", "========== FETCH LEAVE REQUESTS ==========")
                Log.d("LeaveVM", "status filter: ${status ?: "ALL"}")
                
                val response = emsApi.getLeaveRequests(status)
                Log.d("LeaveVM", "Response Code: ${response.code()}")
                Log.d("LeaveVM", "Is Successful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("LeaveVM", "Success! Received ${data.size} leave requests")
                    data.forEachIndexed { index, leave ->
                        Log.d("LeaveVM", "  Request ${index + 1}:")
                        Log.d("LeaveVM", "    leaveApplicationId: ${leave.leaveApplicationId}")
                        Log.d("LeaveVM", "    empId: ${leave.empId}")
                        Log.d("LeaveVM", "    status: ${leave.status}")
                    }
                    Log.d("LeaveVM", "================================================")
                    _leaveRequests.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LeaveVM", "Failed to fetch requests. Code: ${response.code()}")
                    Log.e("LeaveVM", "Error: $errorBody")
                    _errorMessage.value = "Failed to load leave requests: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "========== FETCH LEAVE REQUESTS FAILED ==========")
                Log.e("LeaveVM", "Exception: ${e.message}")
                e.printStackTrace()
                Log.d("LeaveVM", "================================================")
                _errorMessage.value = e.message ?: "Failed to load leave requests"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =====================
    // APPROVE / REJECT LEAVE
    // =====================
    fun approveRejectLeave(leaveApplicationId: String, status: String, remarks: String?) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("LeaveVM", "========== APPROVE/REJECT LEAVE ==========")
                Log.d("LeaveVM", "leaveApplicationId: $leaveApplicationId")
                Log.d("LeaveVM", "status: $status")
                Log.d("LeaveVM", "remarks: $remarks")
                
                val request = LeaveApprovalRequest(
                    leaveApplicationId = leaveApplicationId,
                    status = status,
                    remarks = remarks
                )
                
                val response = emsApi.approveRejectLeave(request)
                Log.d("LeaveVM", "Response Code: ${response.code()}")
                Log.d("LeaveVM", "Is Successful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("LeaveVM", "Success!")
                    Log.d("LeaveVM", "  leaveApplicationId: ${data.leaveApplicationId}")
                    Log.d("LeaveVM", "  status: ${data.status}")
                    Log.d("LeaveVM", "  message: ${data.message}")
                    Log.d("LeaveVM", "================================================")
                    _applyResult.value = true to (data.message ?: "Leave $status successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LeaveVM", "Failed to update leave. Code: ${response.code()}")
                    Log.e("LeaveVM", "Error: $errorBody")
                    Log.d("LeaveVM", "================================================")
                    _applyResult.value = false to ("Failed to update leave: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "========== APPROVE/REJECT LEAVE FAILED ==========")
                Log.e("LeaveVM", "Exception: ${e.message}")
                e.printStackTrace()
                Log.d("LeaveVM", "================================================")
                _applyResult.value = false to (e.message ?: "Failed to update leave")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =====================
    // SINGLE LEAVE DETAILS
    // =====================
    fun fetchLeaveDetails(leaveApplicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("LeaveVM", "========== FETCH LEAVE DETAILS ==========")
                Log.d("LeaveVM", "leaveApplicationId: $leaveApplicationId")
                
                val response = emsApi.getLeaveDetails(leaveApplicationId)
                Log.d("LeaveVM", "Response Code: ${response.code()}")
                Log.d("LeaveVM", "Is Successful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("LeaveVM", "Success! Leave Details:")
                    Log.d("LeaveVM", "  leaveApplicationId: ${data.leaveApplicationId}")
                    Log.d("LeaveVM", "  empId: ${data.empId}")
                    Log.d("LeaveVM", "  leaveType: ${data.leaveType}")
                    Log.d("LeaveVM", "  leaveDay: ${data.leaveDay}")
                    Log.d("LeaveVM", "  startDate: ${data.startDate}")
                    Log.d("LeaveVM", "  endDate: ${data.endDate}")
                    Log.d("LeaveVM", "  noOfDays: ${data.noOfDays}")
                    Log.d("LeaveVM", "  description: ${data.description}")
                    Log.d("LeaveVM", "  status: ${data.status}")
                    Log.d("LeaveVM", "  remarks: ${data.remarks}")
                    Log.d("LeaveVM", "================================================")
                    _leaveDetails.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LeaveVM", "Failed to fetch details. Code: ${response.code()}")
                    Log.e("LeaveVM", "Error: $errorBody")
                    _errorMessage.value = "Failed to load leave details: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("LeaveVM", "========== FETCH LEAVE DETAILS FAILED ==========")
                Log.e("LeaveVM", "Exception: ${e.message}")
                e.printStackTrace()
                Log.d("LeaveVM", "================================================")
                _errorMessage.value = e.message ?: "Failed to load leave details"
            } finally {
                _isLoading.value = false
            }
        }
    }
}