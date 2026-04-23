package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.AttendanceCheckInRequest
import com.example.myapplication.data.model.AttendanceResponse

class AttendanceRepository {

    private val pmsApi = RetrofitClient.pmsApi

    // TODO: Implement getAttendanceHistory when PmsApiService method is available
    suspend fun getAttendanceHistory(empId: String): Result<List<AttendanceResponse>> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.getAttendanceHistory(empId)

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("AttendanceRepo", "Get history failed: $errorBody")
                Result.failure(Exception("Failed to fetch attendance: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Exception in getAttendanceHistory", e)
            Result.failure(e)
        }
    }

    // TODO: Implement markAttendance when PmsApiService method is available
    suspend fun markAttendance(empId: String): Result<AttendanceResponse> {
        return try {
            // Token automatically attached by PMS interceptor
            val request = AttendanceCheckInRequest(empId)
            val response = pmsApi.markAttendance(request)

            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("AttendanceRepo", "Mark attendance failed: $errorBody")
                Result.failure(Exception("Failed to mark attendance: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Exception in markAttendance", e)
            Result.failure(e)
        }
    }
}
