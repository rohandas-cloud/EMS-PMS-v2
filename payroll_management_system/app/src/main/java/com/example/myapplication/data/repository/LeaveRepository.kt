package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.*

class LeaveRepository {

    private val pmsApi = RetrofitClient.pmsApi

    // =========================
    // BALANCE
    // =========================
    suspend fun getLeaveBalance(empId: String): Result<List<LeaveBalanceItem>> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.getLeaveBalance(empId)

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Empty response body"))

                // Map backend response to UI model
                val mappedList = listOf(
                    LeaveBalanceItem(
                        leaveType = "Casual Leave",
                        remainingLeaves = body.casualLeave ?: 0,
                        usedLeaves = 0,
                        totalLeaves = body.casualLeave ?: 0,
                        year = "2026"
                    ),
                    LeaveBalanceItem(
                        leaveType = "Sick Leave",
                        remainingLeaves = body.sickLeave ?: 0,
                        usedLeaves = 0,
                        totalLeaves = body.sickLeave ?: 0,
                        year = "2026"
                    ),
                    LeaveBalanceItem(
                        leaveType = "Earned Leave",
                        remainingLeaves = body.earnedLeave ?: 0,
                        usedLeaves = 0,
                        totalLeaves = body.earnedLeave ?: 0,
                        year = "2026"
                    )
                )
                Result.success(mappedList)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("LeaveRepo", "Balance error: $errorBody")
                Result.failure(Exception("Failed to fetch leave balance: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeaveRepo", "Balance Exception", e)
            Result.failure(e)
        }
    }

    // =========================
    // LEAVE TYPES
    // =========================
    suspend fun getLeaveTypes(): Result<List<LeaveType>> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.getLeaveTypes()

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("LeaveRepo", "Types error: $errorBody")
                Result.failure(Exception("Failed to fetch leave types: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeaveRepo", "Types Exception", e)
            Result.failure(e)
        }
    }

    // =========================
    // APPLY LEAVE
    // =========================
    suspend fun applyLeave(request: LeaveApplyRequest): Result<String> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.applyLeave(request)

            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.message ?: "Leave applied successfully"
                Result.success(message)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("LeaveRepo", "Apply error: $errorBody")
                Result.failure(Exception("Failed to apply leave: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeaveRepo", "Apply Exception", e)
            Result.failure(e)
        }
    }

    // =========================
    // LEAVE HISTORY
    // =========================
    suspend fun getLeaveHistory(): Result<List<LeaveResponse>> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.getLeaveHistory()

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("LeaveRepo", "History error: $errorBody")
                Result.failure(Exception("Failed to fetch leave history: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeaveRepo", "History Exception", e)
            Result.failure(e)
        }
    }
}
