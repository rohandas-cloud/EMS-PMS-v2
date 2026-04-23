package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.AttendanceCheckInRequest
import com.example.myapplication.data.model.AttendanceResponse
import kotlinx.coroutines.launch

class AttendanceViewModel : ViewModel() {
    
    // Use real EMS API client
    private val emsApi = RetrofitClient.emsApi
    
    private val _attendanceHistory = MutableLiveData<List<AttendanceResponse>>()
    val attendanceHistory: LiveData<List<AttendanceResponse>> get() = _attendanceHistory

    private val _todayAttendance = MutableLiveData<AttendanceResponse?>()
    val todayAttendance: LiveData<AttendanceResponse?> get() = _todayAttendance

    private val _monthlyAttendance = MutableLiveData<List<AttendanceResponse>>()
    val monthlyAttendance: LiveData<List<AttendanceResponse>> get() = _monthlyAttendance

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> get() = _statusMessage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Fetch Attendance History
    fun fetchAttendanceHistory(empId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""

            try {
                Log.d("AttendanceVM", "========== FETCH ATTENDANCE HISTORY ==========")
                Log.d("AttendanceVM", "empId: $empId")
                
                // Real API call
                val response = emsApi.getAttendanceHistory(empId)
                Log.d("AttendanceVM", "Response Code: ${response.code()}")
                Log.d("AttendanceVM", "Is Successful: ${response.isSuccessful}")
                Log.d("AttendanceVM", "Response Message: ${response.message()}")
                Log.d("AttendanceVM", "Response Headers: ${response.headers()}")
                Log.d("AttendanceVM", "Response Body: ${response.body()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val attendanceData = response.body()!!
                    Log.d("AttendanceVM", "Success! Received ${attendanceData.size} attendance records")
                    Log.d("AttendanceVM", "Full response body: $attendanceData")
                    
                    attendanceData.forEachIndexed { index, attendance ->
                        Log.d("AttendanceVM", "  Record ${index + 1}:")
                        Log.d("AttendanceVM", "    date: ${attendance.date}")
                        Log.d("AttendanceVM", "    empId: ${attendance.empId}")
                        Log.d("AttendanceVM", "    inTime: ${attendance.inTime}")
                        Log.d("AttendanceVM", "    outTime: ${attendance.outTime}")
                        Log.d("AttendanceVM", "    workingHour: ${attendance.workingHour}")
                        Log.d("AttendanceVM", "    message: ${attendance.message}")
                        Log.d("AttendanceVM", "    status: ${attendance.status}")
                        Log.d("AttendanceVM", "    Raw object: $attendance")
                    }
                    Log.d("AttendanceVM", "================================================")
                    _attendanceHistory.value = attendanceData
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AttendanceVM", "Failed to fetch history. Code: ${response.code()}")
                    Log.e("AttendanceVM", "Error Message: ${response.message()}")
                    Log.e("AttendanceVM", "Error Body: $errorBody")
                    Log.e("AttendanceVM", "Response Headers: ${response.headers()}")
                    _attendanceHistory.value = emptyList()
                    _errorMessage.value = "Failed to load attendance data: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "========== FETCH ATTENDANCE HISTORY FAILED ==========")
                Log.e("AttendanceVM", "Exception: ${e.message}")
                Log.e("AttendanceVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("AttendanceVM", "================================================")
                _attendanceHistory.value = emptyList()
                _errorMessage.value = e.message ?: "Failed to load attendance data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fetch Today's Attendance
    fun fetchTodayAttendance() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""

            try {
                Log.d("AttendanceVM", "========== FETCH TODAY ATTENDANCE ==========")
                
                val response = emsApi.getTodayAttendance()
                Log.d("AttendanceVM", "Response Code: ${response.code()}")
                Log.d("AttendanceVM", "Is Successful: ${response.isSuccessful}")
                Log.d("AttendanceVM", "Response Message: ${response.message()}")
                Log.d("AttendanceVM", "Response Headers: ${response.headers()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("AttendanceVM", "Success! Today's attendance:")
                    Log.d("AttendanceVM", "Full response body: $data")
                    Log.d("AttendanceVM", "  date: ${data.date}")
                    Log.d("AttendanceVM", "  empId: ${data.empId}")
                    Log.d("AttendanceVM", "  inTime: ${data.inTime}")
                    Log.d("AttendanceVM", "  outTime: ${data.outTime}")
                    Log.d("AttendanceVM", "  workingHour: ${data.workingHour}")
                    Log.d("AttendanceVM", "  message: ${data.message}")
                    Log.d("AttendanceVM", "  status: ${data.status}")
                    Log.d("AttendanceVM", "  Raw object: $data")
                    Log.d("AttendanceVM", "================================================")
                    _todayAttendance.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AttendanceVM", "Failed to fetch today's attendance. Code: ${response.code()}")
                    Log.e("AttendanceVM", "Error Message: ${response.message()}")
                    Log.e("AttendanceVM", "Error Body: $errorBody")
                    Log.e("AttendanceVM", "Response Headers: ${response.headers()}")
                    _todayAttendance.value = null
                    _errorMessage.value = "Failed to load today's attendance: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "========== FETCH TODAY ATTENDANCE FAILED ==========")
                Log.e("AttendanceVM", "Exception: ${e.message}")
                Log.e("AttendanceVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("AttendanceVM", "================================================")
                _todayAttendance.value = null
                _errorMessage.value = e.message ?: "Failed to load today's attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fetch Monthly Attendance
    fun fetchMonthlyAttendance(empId: String, year: Int, month: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""

            try {
                Log.d("AttendanceVM", "========== FETCH MONTHLY ATTENDANCE ==========")
                Log.d("AttendanceVM", "empId: $empId, year: $year, month: $month")
                
                val response = emsApi.getMonthlyAttendance(empId, year, month)
                Log.d("AttendanceVM", "Response Code: ${response.code()}")
                Log.d("AttendanceVM", "Is Successful: ${response.isSuccessful}")
                Log.d("AttendanceVM", "Response Message: ${response.message()}")
                Log.d("AttendanceVM", "Response Headers: ${response.headers()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("AttendanceVM", "Success! Received ${data.size} attendance records for month $month/$year")
                    Log.d("AttendanceVM", "Full response body: $data")
                    data.forEachIndexed { index, attendance ->
                        Log.d("AttendanceVM", "  Record ${index + 1}:")
                        Log.d("AttendanceVM", "    date: ${attendance.date}")
                        Log.d("AttendanceVM", "    inTime: ${attendance.inTime}")
                        Log.d("AttendanceVM", "    outTime: ${attendance.outTime}")
                        Log.d("AttendanceVM", "    workingHour: ${attendance.workingHour}")
                        Log.d("AttendanceVM", "    status: ${attendance.status}")
                        Log.d("AttendanceVM", "    Raw object: $attendance")
                    }
                    Log.d("AttendanceVM", "================================================")
                    _monthlyAttendance.value = data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AttendanceVM", "Failed to fetch monthly attendance. Code: ${response.code()}")
                    Log.e("AttendanceVM", "Error Message: ${response.message()}")
                    Log.e("AttendanceVM", "Error Body: $errorBody")
                    Log.e("AttendanceVM", "Response Headers: ${response.headers()}")
                    _monthlyAttendance.value = emptyList()
                    _errorMessage.value = "Failed to load monthly attendance: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "========== FETCH MONTHLY ATTENDANCE FAILED ==========")
                Log.e("AttendanceVM", "Exception: ${e.message}")
                Log.e("AttendanceVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("AttendanceVM", "================================================")
                _monthlyAttendance.value = emptyList()
                _errorMessage.value = e.message ?: "Failed to load monthly attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Mark Attendance (Check-in/Check-out)
    fun markAttendance() {
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        
        if (empId == null) {
            _errorMessage.value = "Employee ID not found. Please login again."
            Log.e("AttendanceVM", "Error: empIdEms is null")
            return
        }

        // Validate empId is not blank
        if (empId.isBlank()) {
            _errorMessage.value = "Invalid Employee ID. Please login again."
            Log.e("AttendanceVM", "Error: empIdEms is blank")
            return
        }

        Log.d("AttendanceVM", "✅ Validated empId: $empId")

        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("AttendanceVM", "========== MARK ATTENDANCE ==========")
                Log.d("AttendanceVM", "Making API call with empId: $empId")
                
                val request = AttendanceCheckInRequest(empId = empId)
                Log.d("AttendanceVM", "Request Body: $request")
                Log.d("AttendanceVM", "Request empId in body: ${request.empId}")
                
                val response = emsApi.markAttendance(request)
                
                Log.d("AttendanceVM", "Response Code: ${response.code()}")
                Log.d("AttendanceVM", "Is Successful: ${response.isSuccessful}")
                Log.d("AttendanceVM", "Response Message: ${response.message()}")
                Log.d("AttendanceVM", "Response Headers: ${response.headers()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d("AttendanceVM", "Success!")
                    Log.d("AttendanceVM", "Full response body: $data")
                    Log.d("AttendanceVM", "  Request empId: $empId")
                    Log.d("AttendanceVM", "  Response empId: ${data.empId}")
                    
                    // Validate response empId matches request
                    if (data.empId != null && data.empId != empId) {
                        Log.w("AttendanceVM", "⚠️ MISMATCH: Request empId ($empId) != Response empId (${data.empId})")
                    }
                    
                    Log.d("AttendanceVM", "  inTime: ${data.inTime}")
                    Log.d("AttendanceVM", "  outTime: ${data.outTime}")
                    Log.d("AttendanceVM", "  workingHour: ${data.workingHour}")
                    Log.d("AttendanceVM", "  message: ${data.message}")
                    Log.d("AttendanceVM", "  status: ${data.status}")
                    Log.d("AttendanceVM", "  Raw object: $data")
                    Log.d("AttendanceVM", "================================================")
                    
                    _statusMessage.value = data.message ?: "Attendance marked successfully"
                    
                    // Refresh history after marking attendance
                    fetchAttendanceHistory(empId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AttendanceVM", "Failed to mark attendance. Code: ${response.code()}")
                    Log.e("AttendanceVM", "Error Message: ${response.message()}")
                    Log.e("AttendanceVM", "Error Body: $errorBody")
                    Log.e("AttendanceVM", "Response Headers: ${response.headers()}")
                    Log.d("AttendanceVM", "================================================")
                    _statusMessage.value = "Failed to mark attendance: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "========== MARK ATTENDANCE FAILED ==========")
                Log.e("AttendanceVM", "Exception: ${e.message}")
                Log.e("AttendanceVM", "Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Log.d("AttendanceVM", "================================================")
                _statusMessage.value = e.message ?: "Failed to mark attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
