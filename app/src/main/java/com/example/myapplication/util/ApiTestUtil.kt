package com.example.myapplication.util

import android.util.Log
import com.example.myapplication.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * API Test Utility - Use this to test if EMS APIs are working
 * Run this from MainActivity or any activity to test API connectivity
 */
object ApiTestUtil {
    
    private const val TAG = "ApiTestUtil"
    
    /**
     * Test all EMS APIs with a sample employee ID
     * Call this from your activity to see detailed logs
     */
    suspend fun testAllEmsApis(empId: String = "TEST123") {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "========== TESTING EMS APIS ==========")
            Log.d(TAG, "Testing with empId: $empId")
            Log.d(TAG, "Using DUMMY API for testing")
            
            try {
                // Test 1: Get Attendance History (DUMMY API)
                Log.d(TAG, "\n--- Test 1: Get Attendance History (DUMMY) ---")
                val attendanceResponse = com.example.myapplication.data.api.DummyApiClient.dummyApi.getAttendanceHistory("1")
                Log.d(TAG, "Attendance History - Code: ${attendanceResponse.code()}")
                Log.d(TAG, "Attendance History - Successful: ${attendanceResponse.isSuccessful}")
                Log.d(TAG, "Attendance History - Body: ${attendanceResponse.body()}")
                if (!attendanceResponse.isSuccessful) {
                    Log.d(TAG, "Attendance History - Error: ${attendanceResponse.errorBody()?.string()}")
                }
                
                // Convert and log the converted data
                if (attendanceResponse.isSuccessful && attendanceResponse.body() != null) {
                    val convertedData = attendanceResponse.body()!!
                    Log.d(TAG, "Received ${convertedData.size} records")
                    convertedData.forEachIndexed { index, data ->
                        Log.d(TAG, "  [$index] date=${data.date}, status=${data.status}, inTime=${data.inTime}")
                    }
                }
                
                Log.d(TAG, "\n========== API TESTS COMPLETED ==========")
                
            } catch (e: Exception) {
                Log.e(TAG, "API Test Failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Test if the base URL is accessible
     */
    suspend fun testBaseUrl() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "========== TESTING BASE URL ==========")
            Log.d(TAG, "EMS Base URL: https://d3lpelprx5afbv.cloudfront.net/")
            
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val request = okhttp3.Request.Builder()
                    .url("https://d3lpelprx5afbv.cloudfront.net/")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                Log.d(TAG, "Base URL Test - Code: ${response.code}")
                Log.d(TAG, "Base URL Test - Message: ${response.message}")
                Log.d(TAG, "Base URL Test - Body preview: ${response.body?.string()?.take(200)}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Base URL Test Failed: ${e.message}")
            }
        }
    }
}
