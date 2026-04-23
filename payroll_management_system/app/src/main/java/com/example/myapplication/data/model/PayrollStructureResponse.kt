package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Payroll Structure Response
 * Salary structure including earnings and deductions mapping
 */
data class PayrollStructure(
    @SerializedName("id")
    val id: String?,
    @SerializedName("salaryComponentId")
    val salaryComponentId: String?,
    @SerializedName("employmentTypeId")
    val employmentTypeId: String?,
    @SerializedName("percentage")
    val percentage: Double?,
    @SerializedName("fixedAmount")
    val fixedAmount: Double?,
    @SerializedName("isOptional")
    val isOptional: Boolean?,
    @SerializedName("isActive")
    val isActive: Boolean?
)

/**
 * Salary Component
 * Individual salary component (Basic, HRA, DA, PF, Tax, etc.)
 */
data class SalaryComponentInfo(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("type")
    val type: String? // "EARNING" or "DEDUCTION"
)

