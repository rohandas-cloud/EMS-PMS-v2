package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.*
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.LeaveRepository
import kotlinx.coroutines.launch

class LeaveViewModel : ViewModel() {

    private val pmsApi = RetrofitClient.pmsApi
    private val emsApi = RetrofitClient.emsApi
    private val repository = LeaveRepository(pmsApi, emsApi)

    private val _leaveBalances = MutableLiveData<LeaveBalanceResponse?>()
    val leaveBalances: LiveData<LeaveBalanceResponse?> = _leaveBalances

    private val _leaveBalancesList = MutableLiveData<List<LeaveBalanceItem>>()
    val leaveBalancesList: LiveData<List<LeaveBalanceItem>> = _leaveBalancesList

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

    fun fetchLeaveBalance() {
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        if (empId == null) {
            _errorMessage.value = "EMS login required for leave balance."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getLeaveBalance(empId)
                if (result.isSuccess) {
                    val dataList = result.getOrNull() ?: emptyList()
                    _leaveBalancesList.value = dataList
                    
                    var casual = 0
                    var sick = 0
                    var earned = 0
                    var total = 0.0
                    var foundEmpId: String? = null

                    dataList.forEach { item ->
                        foundEmpId = item.empLeaveId
                        val remaining = item.remainingLeaves ?: 0.0
                        total += remaining
                        when {
                            item.leaveType?.contains("Casual", ignoreCase = true) == true -> casual = remaining.toInt()
                            item.leaveType?.contains("Sick", ignoreCase = true) == true -> sick = remaining.toInt()
                            item.leaveType?.contains("Earned", ignoreCase = true) == true -> earned = remaining.toInt()
                        }
                    }

                    _leaveBalances.value = LeaveBalanceResponse(
                        empId = foundEmpId ?: empId,
                        casualLeave = casual,
                        sickLeave = sick,
                        earnedLeave = earned,
                        totalLeave = total.toInt(),
                        message = "Success"
                    )
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load balance"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchLeaveTypes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getLeaveTypes()
                if (result.isSuccess) {
                    _leaveTypes.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load types"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyLeave(request: LeaveApplyRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.applyLeave(request)
                if (result.isSuccess) {
                    _applyResult.value = true to (result.getOrNull() ?: "Leave applied successfully")
                } else {
                    _applyResult.value = false to (result.exceptionOrNull()?.message ?: "Failed to apply leave")
                }
            } catch (e: Exception) {
                _applyResult.value = false to (e.message ?: "Error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchLeaveHistory() {
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        if (empId == null) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getLeaveHistory(empId)
                if (result.isSuccess) {
                    _leaveHistory.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load history"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}