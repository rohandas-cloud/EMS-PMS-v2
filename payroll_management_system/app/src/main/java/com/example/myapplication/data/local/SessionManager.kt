package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        // Token Keys
        private const val TOKEN_PMS = "token_pms"
        private const val TOKEN_EMS = "token_ems"

        // Employee ID Keys
        private const val EMP_ID_PMS = "emp_id_pms"
        private const val EMP_ID_EMS = "emp_id_ems"

        // User Info Keys
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_EMAIL = "user_email"

        // Session Metadata
        private const val LOGIN_TIMESTAMP = "login_timestamp"
        private const val IS_LOGGED_IN = "is_logged_in"
    }

    // =========================
    // PMS TOKEN
    // =========================
    fun savePmsToken(token: String) {
        prefs.edit().putString(TOKEN_PMS, token).apply()
    }

    fun fetchPmsToken(): String? =
        prefs.getString(TOKEN_PMS, null)

    fun clearPmsToken() {
        prefs.edit().remove(TOKEN_PMS).apply()
    }

    // =========================
    // EMS TOKEN
    // =========================
    fun saveEmsToken(token: String) {
        prefs.edit().putString(TOKEN_EMS, token).apply()
    }

    fun fetchEmsToken(): String? =
        prefs.getString(TOKEN_EMS, null)

    fun clearEmsToken() {
        prefs.edit().remove(TOKEN_EMS).apply()
    }

    // =========================
    // EMPLOYEE ID (PMS)
    // =========================
    fun saveEmpIdPms(empId: String) {
        prefs.edit().putString(EMP_ID_PMS, empId).apply()
    }

    fun fetchEmpIdPms(): String? =
        prefs.getString(EMP_ID_PMS, null)

    // =========================
    // EMPLOYEE ID (EMS)
    // =========================
    fun saveEmpIdEms(empId: String) {
        prefs.edit().putString(EMP_ID_EMS, empId).apply()
        android.util.Log.d("SessionManager", "EMS empId saved: $empId")
    }

    fun fetchEmpIdEms(): String? {
        val empId = prefs.getString(EMP_ID_EMS, null)
        android.util.Log.d("SessionManager", "Fetching EMS empId: $empId")
        return empId
    }

    /**
     * Validate that the provided empId matches the one stored in session
     * @param empId The employee ID to validate
     * @return true if empId matches session, false otherwise
     */
    fun validateEmpIdEms(empId: String?): Boolean {
        val sessionEmpId = fetchEmpIdEms()
        val isValid = empId != null && sessionEmpId != null && empId == sessionEmpId
        android.util.Log.d("SessionManager", "Validating EMS empId: provided=$empId, session=$sessionEmpId, valid=$isValid")
        return isValid
    }

    /**
     * Get EMS empId with validation - returns null if empId is empty or invalid
     * @return Validated EMS empId or null
     */
    fun getValidatedEmpIdEms(): String? {
        val empId = fetchEmpIdEms()
        if (empId.isNullOrBlank()) {
            android.util.Log.w("SessionManager", "EMS empId is null or blank")
            return null
        }
        android.util.Log.d("SessionManager", "Validated EMS empId: $empId")
        return empId
    }

    // =========================
    // USER ID
    // =========================
    fun saveUserId(id: String) {
        prefs.edit().putString(USER_ID, id).apply()
    }

    fun fetchUserId(): String? =
        prefs.getString(USER_ID, null)

    // =========================
    // USER NAME
    // =========================
    fun saveUserName(name: String) {
        prefs.edit().putString(USER_NAME, name).apply()
    }

    fun fetchUserName(): String? =
        prefs.getString(USER_NAME, null)

    // =========================
    // USER EMAIL
    // =========================
    fun saveUserEmail(email: String) {
        prefs.edit().putString(USER_EMAIL, email).apply()
    }

    fun fetchUserEmail(): String? =
        prefs.getString(USER_EMAIL, null)

    // =========================
    // SESSION METADATA
    // =========================
    fun saveLoginTimestamp() {
        prefs.edit().putLong(LOGIN_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun fetchLoginTimestamp(): Long =
        prefs.getLong(LOGIN_TIMESTAMP, 0)

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean =
        prefs.getBoolean(IS_LOGGED_IN, false)

    // =========================
    // CHECK IF BOTH TOKENS EXISTS
    // =========================
    fun hasBothTokens(): Boolean {
        return !fetchPmsToken().isNullOrEmpty() && !fetchEmsToken().isNullOrEmpty()
    }

    // =========================
    // CLEAR SESSION
    // =========================
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // =========================
    // CLEAR ONLY TOKENS (Keep user info)
    // =========================
    fun clearTokensOnly() {
        prefs.edit().apply {
            remove(TOKEN_PMS)
            remove(TOKEN_EMS)
            remove(LOGIN_TIMESTAMP)
            remove(IS_LOGGED_IN)
        }.apply()
    }

    // =========================
    // DEPRECATED METHODS (For backward compatibility)
    // =========================
    @Deprecated("Use savePmsToken instead", ReplaceWith("savePmsToken(token)"))
    fun savePrimaryToken(token: String) {
        savePmsToken(token)
    }

    @Deprecated("Use fetchPmsToken instead", ReplaceWith("fetchPmsToken()"))
    fun fetchPrimaryToken(): String? =
        fetchPmsToken()

    @Deprecated("Use saveEmsToken instead", ReplaceWith("saveEmsToken(token)"))
    fun saveSecondaryToken(token: String) {
        saveEmsToken(token)
    }

    @Deprecated("Use fetchEmsToken instead", ReplaceWith("fetchEmsToken()"))
    fun fetchSecondaryToken(): String? =
        fetchEmsToken()

    @Deprecated("Use saveEmpIdPms instead", ReplaceWith("saveEmpIdPms(empId)"))
    fun saveEmpIdPrimary(empId: String) {
        saveEmpIdPms(empId)
    }

    @Deprecated("Use fetchEmpIdPms instead", ReplaceWith("fetchEmpIdPms()"))
    fun fetchEmpIdPrimary(): String? =
        fetchEmpIdPms()

    @Deprecated("Use saveEmpIdEms instead", ReplaceWith("saveEmpIdEms(empId)"))
    fun saveEmpIdSecondary(empId: String) {
        saveEmpIdEms(empId)
    }

    @Deprecated("Use fetchEmpIdEms instead", ReplaceWith("fetchEmpIdEms()"))
    fun fetchEmpIdSecondary(): String? =
        fetchEmpIdEms()
}