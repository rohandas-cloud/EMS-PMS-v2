package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models for additional PMS APIs
 */

// --- Pay Structure ---
data class PayStructure(
    val id: String? = null,
    val salaryComponentId: String?,
    val employmentTypeId: String?,
    val percentage: Double?,
    val fixedAmount: Double?,
    val calculationType: String? = "PERCENTAGE",
    val calculationBase: String? = "CTC",
    val isOptional: Boolean? = false,
    val isActive: Boolean? = true
)

data class PayStructureResponse(
    val data: PayStructure?,
    val message: String?
)

// --- Revision Types ---
data class RevisionType(
    val id: String? = null,
    val revisionName: String?,
    val category: String?, // "BONUS", "REIMBURSEMENT", etc.
    val isActive: Boolean? = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// --- Payroll Cycle ---
data class PayrollCycle(
    val id: String? = null,
    val cycleName: String?,
    val startDate: String?,
    val endDate: String?,
    val payoutDate: String?,
    val isActive: Boolean? = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// --- Tax Slab ---
data class TaxSlab(
    val id: String? = null,
    val minIncome: Double?,
    val maxIncome: Double?,
    val taxPercentage: Double?,
    val financialYear: String?,
    val isActive: Boolean? = true
)

// --- Employee Pay Structure Assignment ---
data class EmpPayStructureAssign(
    val empId: String,
    val payStructureId: String
)

data class EmpPayStructureResponse(
    val data: EmpPayStructureData?,
    val message: String?
)

data class EmpPayStructureData(
    val empId: String?,
    val empPayStructId: String?,
    val payStructureId: String?
)

// --- Payroll Run ---
data class PayrollRunRequest(
    val month: Int,
    val year: Int,
    val payCycleId: String
)

data class PayrollRunResponse(
    val data: PayrollRunData?,
    val message: String?
)

data class PayrollRunData(
    val month: Int?,
    val year: Int?,
    val payRollDetailsId: String?,
    val status: String? // "INITIATED", "SUCCESS", etc.
)

// --- Refresh Token ---
data class TokenRefreshResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val user: UserInfo?
)
