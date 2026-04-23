package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

// --- Summary API Models ---
data class PayrollSummaryRequest(
    val month: Int?,
    val year: Int?,
    val deptId: String? = null,
    val empId: String,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "netSalary",
    val sortDir: String = "desc"
)

data class PayrollSummaryResponse(
    val content: List<PayrollSummaryItem>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean
)

data class PayrollSummaryItem(
    val empSalaryId: String,
    val firstName: String?,
    val lastName: String?,
    val netSalary: Double?,
    val grossSalary: Double?,
    val totalDeductions: Double?,
    val month: Int?,
    val year: Int?,
    val status: String?
)

// --- Detail API Model ---
data class PayrollDetailResponse(
    val empSalaryId: String,
    val basicSalary: Double?,
    val hra: Double?,
    val allowances: Double?,
    val grossSalary: Double?,
    val totalDeductions: Double?,
    val netSalary: Double?,
    val status: String?,
    // Keeping support for dynamic components if needed by UI
    val components: List<SalaryComponent>? = null
)

// --- Employee Profile Model ---
data class EmployeeProfileResponse(
    val empId: String,
    val name: String?,
    val email: String?
)
