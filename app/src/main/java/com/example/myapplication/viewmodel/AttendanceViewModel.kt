package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class AttendanceViewModel : ViewModel() {
    
    private val emsApi = RetrofitClient.emsApi
    private val pmsApi = RetrofitClient.pmsApi
    
    private val _attendanceHistory = MutableLiveData<List<AttendanceResponse>>()
    val attendanceHistory: LiveData<List<AttendanceResponse>> get() = _attendanceHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> get() = _statusMessage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _dailyAttendance = MutableLiveData<AttendanceResponse?>()
    val dailyAttendance: LiveData<AttendanceResponse?> get() = _dailyAttendance

    fun fetchAttendanceHistory(empId: String) {
        Log.d("AttendanceVM", "Fetching history for empId: $empId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = emsApi.getAttendanceHistory(empId)
                Log.d("AttendanceVM", "History Response: ${response.code()} Body items: ${response.body()?.size ?: 0}")
                
                if (response.isSuccessful) {
                    val contentType = response.headers()["Content-Type"] ?: ""
                    if (contentType.contains("text/html", ignoreCase = true)) {
                        Log.e("AttendanceVM", "Received HTML instead of JSON for history - likely wrong endpoint. Falling back to PMS")
                        fetchAttendanceHistoryFromPms(empId)
                        return@launch
                    }
                    _attendanceHistory.value = response.body() ?: emptyList()
                } else {
                    val errorStr = response.errorBody()?.string() ?: ""
                    Log.e("AttendanceVM", "History Error ($empId): $errorStr")
                    
                    if (response.code() == 404 || response.code() == 400 || errorStr.contains("not found", ignoreCase = true)) {
                        Log.e("AttendanceVM", "EMS not found. Falling back to PMS")
                        fetchAttendanceHistoryFromPms(empId)
                    } else {
                        Log.e("AttendanceVM", "EMS error. Falling back to PMS")
                        fetchAttendanceHistoryFromPms(empId)
                    }
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "History Exception", e)
                Log.e("AttendanceVM", "EMS Exception. Falling back to PMS")
                fetchAttendanceHistoryFromPms(empId)
            }
        }
    }

    private fun fetchAttendanceHistoryFromPms(empId: String) {
        viewModelScope.launch {
            try {
                val response = pmsApi.getAttendanceHistory(empId)
                if (response.isSuccessful) {
                    _attendanceHistory.value = response.body() ?: emptyList()
                } else {
                    _attendanceHistory.value = emptyList()
                    _errorMessage.value = "History Failed: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "PMS History Exception", e)
                _attendanceHistory.value = emptyList()
                _errorMessage.value = "Connection Error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchDailyAttendance(empId: String, date: String) {
        Log.d("AttendanceVM", "Fetching daily attendance for $empId on $date")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = emsApi.getDailyAttendance(empId, date)
                Log.d("AttendanceVM", "Daily Response: ${response.code()}")
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("AttendanceVM", "Daily Data: In=${data?.inTime}, Out=${data?.outTime}")
                    _dailyAttendance.value = data
                } else {
                    Log.e("AttendanceVM", "Daily Fetch Failed: ${response.code()}")
                    _dailyAttendance.value = null
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "Daily Exception", e)
                _dailyAttendance.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAttendance() {
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        Log.d("AttendanceVM", "Marking attendance for empId: $empId")
        
        if (empId.isNullOrBlank()) {
            _errorMessage.value = "Employee ID not found"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = AttendanceCheckInRequest(empId = empId)
                val response = emsApi.markAttendance(request)
                Log.d("AttendanceVM", "Mark Attendance Response: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val msg = body?.message ?: "Success: Checked In/Out"
                    Log.d("AttendanceVM", "Attendance success message: $msg")
                    _statusMessage.value = msg
                    
                    // Refresh data
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    fetchDailyAttendance(empId, today)
                    fetchAttendanceHistory(empId)
                } else {
                    val errorStr = response.errorBody()?.string() ?: ""
                    Log.e("AttendanceVM", "Mark Attendance Error: $errorStr")
                    val message = try {
                        JSONObject(errorStr).getString("message")
                    } catch (e: Exception) {
                        "Action Failed (${response.code()})"
                    }
                    _statusMessage.value = message
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "Mark Attendance Exception", e)
                _statusMessage.value = "Connection Error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
