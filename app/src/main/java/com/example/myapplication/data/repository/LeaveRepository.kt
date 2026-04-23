package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.*
import com.example.myapplication.data.model.*

class LeaveRepository(
    private val pmsApi: PmsApiService,
    private val emsApi: EmsApiService
) {

    // =========================
    // BALANCE
    // =========================
    suspend fun getLeaveBalance(empId: String): Result<List<LeaveBalanceItem>> {
        return try {
            // Try EMS API first
            val response = emsApi.getLeaveBalance(empId)

            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!)
            } else {
                // Fallback to PMS API
                val pmsResponse = pmsApi.getLeaveBalance(empId)
                if (pmsResponse.isSuccessful) {
                    val body = pmsResponse.body()
                    val mappedList = listOf(
                        LeaveBalanceItem(
                            leaveType = "Casual Leave",
                            remainingLeaves = (body?.casualLeave ?: 0).toDouble(),
                            totalLeaves = (body?.casualLeave ?: 0).toDouble(),
                            year = "2026"
                        ),
                        LeaveBalanceItem(
                            leaveType = "Sick Leave",
                            remainingLeaves = (body?.sickLeave ?: 0).toDouble(),
                            totalLeaves = (body?.sickLeave ?: 0).toDouble(),
                            year = "2026"
                        )
                    )
                    Result.success(mappedList)
                } else {
                    Result.failure(Exception("Balance fetch failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================
    // LEAVE TYPES
    // =========================
    suspend fun getLeaveTypes(): Result<List<LeaveType>> {
        return try {
            val response = emsApi.getLeaveTypes()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!)
            } else {
                val pmsResponse = pmsApi.getLeaveTypes()
                if (pmsResponse.isSuccessful && !pmsResponse.body().isNullOrEmpty()) {
                    Result.success(pmsResponse.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch leave types"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================
    // APPLY LEAVE
    // =========================
    suspend fun applyLeave(request: LeaveApplyRequest): Result<String> {
        return try {
            // Try Allocate endpoint first
            val response = emsApi.applyLeave(request)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Applied")
            } else {
                // Fallback to Standard Apply endpoint
                Log.d("LeaveRepo", "Allocate failed, trying Standard Apply...")
                val standardResponse = emsApi.applyLeaveStandard(request)
                if (standardResponse.isSuccessful) {
                    Result.success(standardResponse.body()?.message ?: "Applied")
                } else {
                    val errorMsg = standardResponse.errorBody()?.string() ?: response.errorBody()?.string() ?: "Apply failed"
                    Log.e("LeaveRepo", "Apply Leave Error: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================
    // LEAVE HISTORY
    // =========================
    suspend fun getLeaveHistory(empId: String): Result<List<LeaveResponse>> {
        return try {
            val response = emsApi.getLeaveHistory(empId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("History fetch failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
