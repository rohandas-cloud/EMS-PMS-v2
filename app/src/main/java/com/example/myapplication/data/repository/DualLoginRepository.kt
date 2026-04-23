package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.EmsApiService
import com.example.myapplication.data.api.PmsApiService
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.MyApplication

/**
 * Dual Login Repository
 * Coordinates login across both PMS and EMS systems
 */
class DualLoginRepository(
    private val pmsApi: PmsApiService,
    private val emsApi: EmsApiService
) {

    private val loginRepository = LoginRepository(pmsApi)

    /**
     * Perform dual login to both PMS and EMS systems
     * Returns a Triple of (PMS Response, EMS Response, Combined Error if any)
     */
    suspend fun performDualLogin(
        email: String,
        password: String
    ): DualLoginResult {
        // Most backends expect 'username' and 'password'. 
        // We'll try sending email in the username field for PMS.
        val pmsRequest = LoginRequest(email = email, password = password, username = email)
        val emsRequest = LoginRequest(email = email, password = password, username = email)

        var pmsResult: Result<LoginResponse>? = null
        var emsResult: Result<LoginResponse>? = null
        var errors = mutableListOf<String>()

        // Step 1: Login to PMS
        try {
            Log.d("DualLoginRepo", "Attempting PMS login for: $email")
            val pmsResponse = loginRepository.login(pmsRequest)

                if (pmsResponse != null && !pmsResponse.accessToken.isNullOrEmpty()) {
                    pmsResult = Result.success(pmsResponse)
                    MyApplication.sessionManager.savePmsToken(pmsResponse.accessToken)
                    
                    val pmsEmpIdFromResponse = pmsResponse.user?.employeeId
                    val (_, pmsEmpIdFromToken, _) = decodeToken(pmsResponse.accessToken)
                    val finalPmsEmpId = pmsEmpIdFromResponse ?: pmsEmpIdFromToken

                    if (finalPmsEmpId != null) {
                        MyApplication.sessionManager.saveEmpIdPms(finalPmsEmpId)
                        Log.d("DualLoginRepo", "PMS login successful. empId: $finalPmsEmpId")
                    } else {
                        Log.w("DualLoginRepo", "PMS login successful, but empId is missing from both response and token.")
                    }
                } else {
                pmsResult = Result.failure(Exception("PMS login failed: Empty response or token"))
                errors.add("PMS login failed")
                Log.e("DualLoginRepo", "PMS login failed")
            }
        } catch (e: Exception) {
            pmsResult = Result.failure(e)
            errors.add("PMS error: ${e.message}")
            Log.e("DualLoginRepo", "PMS login exception", e)
        }

        // Step 2: Login to EMS (only if PMS succeeded, or attempt anyway based on your requirement)
        try {
            Log.d("DualLoginRepo", "Attempting EMS login for: $email")
            val emsResponse = emsApi.loginUser(emsRequest)

            if (emsResponse.isSuccessful) {
                val emsBody = emsResponse.body()
                if (emsBody != null && !emsBody.accessToken.isNullOrEmpty()) {
                    emsResult = Result.success(emsBody)
                    MyApplication.sessionManager.saveEmsToken(emsBody.accessToken)
                    val emsEmpIdFromResponse = emsBody.user?.employeeId
                    val (_, emsEmpIdFromToken, _) = decodeToken(emsBody.accessToken!!)
                    val finalEmsEmpId = emsEmpIdFromResponse ?: emsEmpIdFromToken

                    if (finalEmsEmpId != null) {
                        MyApplication.sessionManager.saveEmpIdEms(finalEmsEmpId)
                        Log.d("DualLoginRepo", "EMS login successful. empId: $finalEmsEmpId")
                    } else {
                        Log.w("DualLoginRepo", "EMS login successful, but empId is missing from both response and token.")
                    }
                } else {
                    emsResult = Result.failure(Exception("EMS login failed: Empty response or token"))
                    errors.add("EMS login failed")
                    Log.e("DualLoginRepo", "EMS login failed")
                }
            } else {
                emsResult = Result.failure(Exception("EMS login failed: ${emsResponse.code()} - ${emsResponse.message()}"))
                errors.add("EMS error: ${emsResponse.code()}")
                Log.e("DualLoginRepo", "EMS login failed with code: ${emsResponse.code()}")
            }
        } catch (e: Exception) {
            emsResult = Result.failure(e)
            errors.add("EMS error: ${e.message}")
            Log.e("DualLoginRepo", "EMS login exception", e)
        }

        // Step 3: Return combined result
        return DualLoginResult(
            pmsResult = pmsResult,
            emsResult = emsResult,
            errors = errors,
            isFullySuccessful = pmsResult?.isSuccess == true && emsResult?.isSuccess == true,
            isPartiallySuccessful = (pmsResult?.isSuccess == true) != (emsResult?.isSuccess == true) // XOR
        )
    }

    /**
     * Refresh both tokens
     */
    suspend fun refreshBothTokens(
        email: String,
        password: String
    ): DualLoginResult {
        Log.d("DualLoginRepo", "Refreshing tokens for: $email")
        return performDualLogin(email, password)
    }

    /**
     * Decode JWT token to extract user information
     */
    fun decodeToken(token: String): Triple<String?, String?, String?> {
        return LoginRepository.decodeToken(token)
    }
}

/**
 * Data class representing the result of dual login
 */
data class DualLoginResult(
    val pmsResult: Result<LoginResponse>?,
    val emsResult: Result<LoginResponse>?,
    val errors: List<String>,
    val isFullySuccessful: Boolean,
    val isPartiallySuccessful: Boolean
) {
    val isSuccess: Boolean
        get() = isFullySuccessful || isPartiallySuccessful

    val pmsResponse: LoginResponse?
        get() = pmsResult?.getOrNull()

    val emsResponse: LoginResponse?
        get() = emsResult?.getOrNull()

    val pmsToken: String?
        get() = pmsResponse?.accessToken

    val emsToken: String?
        get() = emsResponse?.accessToken

    val errorMessage: String
        get() = errors.joinToString("; ")
}
