package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.myapplication.MyApplication
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.repository.DualLoginRepository
import com.example.myapplication.data.repository.DualLoginResult
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val dualLoginRepository = DualLoginRepository(
        pmsApi = RetrofitClient.pmsApi,
        emsApi = RetrofitClient.emsApi
    )

    // Login state
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    private val _logoutResult = MutableLiveData<Result<Boolean>>()
    val logoutResult: LiveData<Result<Boolean>> get() = _logoutResult

    // =========================================================
    // DUAL LOGIN (PMS + EMS)
    // =========================================================
    fun dualLogin(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                Log.d("LoginViewModel", "Starting dual login for: $email")

                // Perform dual login
                val result = dualLoginRepository.performDualLogin(email, password)

                when {
                    result.isFullySuccessful -> {
                        // Both logins succeeded
                        handleSuccessfulDualLogin(result)
                        _loginState.value = LoginState.Success(result)
                    }
                    result.isPartiallySuccessful -> {
                        // One login succeeded, one failed
                        handlePartialLogin(result)
                        _loginState.value = LoginState.PartialSuccess(result)
                    }
                    else -> {
                        // Both logins failed
                        _loginState.value = LoginState.Error(result.errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Dual login exception", e)
                _loginState.value = LoginState.Error("Login failed: ${e.message}")
            }
        }
    }

    /**
     * Handle successful dual login (both PMS and EMS)
     */
    private fun handleSuccessfulDualLogin(result: DualLoginResult) {
        try {
            // PMS part
            result.pmsToken?.let { token ->
                MyApplication.sessionManager.savePmsToken(token)

                // Decode PMS token
                val (_, pmsEmpIdFromToken, _) = dualLoginRepository.decodeToken(token)
                val pmsEmpIdFromResponse = result.pmsResponse?.user?.employeeId
                val finalPmsEmpId = pmsEmpIdFromResponse ?: pmsEmpIdFromToken

                finalPmsEmpId?.let { id ->
                    MyApplication.sessionManager.saveEmpIdPms(id)
                    Log.d("LoginViewModel", "✅ PMS empId saved: $id")
                    
                    // Verify save worked
                    val verify = MyApplication.sessionManager.fetchEmpIdPms()
                    Log.d("LoginViewModel", "✅ Verify after save: $verify")
                } ?: run {
                    Log.e("LoginViewModel", "❌ PMS empId is NULL - cannot save!")
                    Log.e("LoginViewModel", "  - Response has employeeId: ${pmsEmpIdFromResponse != null}")
                    Log.e("LoginViewModel", "  - Token has empId claim: ${pmsEmpIdFromToken != null}")
                }
            }

            // EMS part
            result.emsToken?.let { token ->
                MyApplication.sessionManager.saveEmsToken(token)

                // Decode EMS token
                val (_, emsEmpIdFromToken, _) = dualLoginRepository.decodeToken(token)
                emsEmpIdFromToken?.let { id ->
                    MyApplication.sessionManager.saveEmpIdEms(id)
                    Log.d("LoginViewModel", "✅ EMS empId saved: $id")
                } ?: run {
                    Log.e("LoginViewModel", "❌ EMS token has no empId claim")
                }
            }

            // Save user info if available
            result.pmsResponse?.user?.userName?.let {
                MyApplication.sessionManager.saveUserName(it)
            }

            MyApplication.sessionManager.saveLoginTimestamp()
            MyApplication.sessionManager.setLoggedIn(true)

            Log.d("LoginViewModel", "Dual login completed successfully")
            Log.d("LoginViewModel", "PMS empId: ${MyApplication.sessionManager.fetchEmpIdPms()}")
            Log.d("LoginViewModel", "EMS empId: ${MyApplication.sessionManager.fetchEmpIdEms()}")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error saving dual login data", e)
        }
    }

    /**
     * Handle partial login (one system succeeded, one failed)
     */
    private fun handlePartialLogin(result: DualLoginResult) {
        try {
            // PMS part
            result.pmsToken?.let { token ->
                MyApplication.sessionManager.savePmsToken(token)
                val (_, pmsEmpId, _) = dualLoginRepository.decodeToken(token)
                pmsEmpId?.let { id ->
                    MyApplication.sessionManager.saveEmpIdPms(id)
                    Log.d("LoginViewModel", "✅ PMS empId saved (partial): $id")
                } ?: Log.e("LoginViewModel", "❌ PMS token missing empId (partial)")
            }

            // EMS part
            result.emsToken?.let { token ->
                MyApplication.sessionManager.saveEmsToken(token)
                Log.d("LoginViewModel", "EMS token saved (partial login)")

                val (_, emsEmpId, _) = dualLoginRepository.decodeToken(token)
                emsEmpId?.let { id ->
                    MyApplication.sessionManager.saveEmpIdEms(id)
                    Log.d("LoginViewModel", "✅ EMS empId saved (partial login): $id")
                } ?: Log.e("LoginViewModel", "❌ EMS token missing empId (partial login)")
            }

            // Save user info if available
            result.pmsResponse?.user?.userName?.let {
                MyApplication.sessionManager.saveUserName(it)
            }

            MyApplication.sessionManager.saveLoginTimestamp()
            MyApplication.sessionManager.setLoggedIn(true)

            Log.w("LoginViewModel", "Partial login completed: ${result.errorMessage}")
            Log.w("LoginViewModel", "PMS empId: ${MyApplication.sessionManager.fetchEmpIdPms()}")
            Log.w("LoginViewModel", "EMS empId: ${MyApplication.sessionManager.fetchEmpIdEms()}")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error saving partial login data", e)
        }
    }

    // =========================================================
    // LEGACY SINGLE LOGIN (For backward compatibility)
    // =========================================================
    @Deprecated("Use dualLogin instead")
    fun login(email: String, password: String) {
        dualLogin(email, password)
    }

    // =========================================================
    // LOGOUT
    // =========================================================
    fun logout() {
        viewModelScope.launch {
            try {
                MyApplication.sessionManager.clearSession()
                _logoutResult.value = Result.success(true)
            } catch (e: Exception) {
                _logoutResult.value = Result.failure(e)
            }
        }
    }
}

/**
 * Sealed class representing different login states
 */
sealed class LoginState {
    object Loading : LoginState()
    data class Success(val result: DualLoginResult) : LoginState()
    data class PartialSuccess(val result: DualLoginResult) : LoginState()
    data class Error(val message: String) : LoginState()
}
