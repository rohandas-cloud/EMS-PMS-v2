package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Holiday

class HolidayRepository {

    private val pmsApi = RetrofitClient.pmsApi

    // =========================================================
    // GET HOLIDAYS (PMS API)
    // =========================================================
    suspend fun getMyHoliday(): Result<List<Holiday>> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.getHolidays()

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("HolidayRepo", "Error: $errorBody")
                Result.failure(Exception("Failed to fetch holidays: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("HolidayRepo", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
