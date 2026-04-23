package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Holiday
import com.example.myapplication.data.model.HolidayResponse

class HolidayRepository {

    private val pmsApi = RetrofitClient.pmsApi
    private val emsApi = RetrofitClient.emsApi

    suspend fun getHolidays(): Result<List<Holiday>> {
        return try {
            // 1. Try EMS API (Primary)
            Log.d("HolidayRepo", "Attempting to fetch holidays from EMS...")
            val response = emsApi.getHolidays()
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Log.d("HolidayRepo", "EMS Holidays found: ${response.body()?.size}")
                val mappedHolidays = response.body()!!.map { res ->
                    Holiday(
                        date = res.holidayDate ?: "N/A",
                        name = res.holidayName ?: "Holiday",
                        location = "General",
                        type = res.holidayType ?: "Public"
                    )
                }
                Result.success(mappedHolidays)
            } else {
                // 2. Fallback to PMS API if EMS is empty or unsuccessful
                Log.d("HolidayRepo", "EMS Holidays empty or failed (${response.code()}), falling back to PMS...")
                val pmsResponse = pmsApi.getHolidays()
                if (pmsResponse.isSuccessful) {
                    val pmsList = pmsResponse.body() ?: emptyList()
                    Log.d("HolidayRepo", "PMS Holidays found: ${pmsList.size}")
                    Result.success(pmsList)
                } else {
                    Log.e("HolidayRepo", "PMS Fetch failed: ${pmsResponse.code()}")
                    Result.failure(Exception("Failed to fetch holidays from both sources"))
                }
            }
        } catch (e: Exception) {
            Log.e("HolidayRepo", "Critical Error fetching holidays: ${e.message}")
            // Final fallback attempt in case of network exception on EMS
            try {
                val pmsResponse = pmsApi.getHolidays()
                if (pmsResponse.isSuccessful) {
                    Result.success(pmsResponse.body() ?: emptyList())
                } else {
                    Result.failure(e)
                }
            } catch (pmsEx: Exception) {
                Result.failure(e)
            }
        }
    }

    // Legacy method for backward compatibility
    suspend fun getMyHoliday(): Result<List<Holiday>> = getHolidays()
}
