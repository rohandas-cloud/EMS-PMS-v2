package com.example.myapplication.data.repository

import android.util.Base64
import android.util.Log
import com.example.myapplication.data.api.PmsApiService
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import org.json.JSONObject

class LoginRepository(
    private val apiService: PmsApiService
) {

    // ============================
    // PRIMARY LOGIN
    // ============================
    suspend fun login(request: LoginRequest): LoginResponse? {
        return try {

            Log.d("LoginRepository", "Login attempt: ${request.email}")

            val response = apiService.loginUser(request)

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("LoginRepository", response.errorBody()?.string() ?: "Login error")
                null
            }

        } catch (e: Exception) {
            Log.e("LoginRepository", e.message ?: "Exception")
            null
        }
    }

    // ============================
    // SECONDARY LOGIN
    // ============================
    suspend fun loginSecondary(request: LoginRequest): LoginResponse? {
        return try {

            val response = apiService.loginUser(request)

            if (response.isSuccessful) response.body()
            else null

        } catch (e: Exception) {
            null
        }
    }

    // ============================
    // LOGOUT
    // ============================
    suspend fun logout(): Boolean {
        return try {
            val response = apiService.loginUser(LoginRequest("", "", ""))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        // =========================================================
        // JWT DECODE (Utility function)
        // =========================================================
        fun decodeToken(token: String): Triple<String?, String?, String?> {
            return try {
                val parts = token.split(".")
                if (parts.size < 2) return Triple(null, null, null)
                
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)

                // Use null if key is missing or value is empty/null string
                fun getStringOrNull(key: String): String? {
                    if (!json.has(key)) return null
                    val value = json.optString(key)
                    return if (value.isNullOrBlank() || value == "null") null else value
                }

                val userId = getStringOrNull("userId") ?: getStringOrNull("id")
                val empId = getStringOrNull("empId") ?: getStringOrNull("employeeId") ?: getStringOrNull("emp_id")
                val email = getStringOrNull("sub") ?: getStringOrNull("email")

                Triple(userId, empId, email)
            } catch (e: Exception) {
                Log.e("LoginRepo", "Decode error: ${e.message}")
                Triple(null, null, null)
            }
        }
    }
}