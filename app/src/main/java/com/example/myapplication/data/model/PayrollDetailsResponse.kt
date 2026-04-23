package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Payroll Details Response
 * Complete payroll details of a specific employee for a given month and year
 */
data class PayrollDetailsResponse(
    @SerializedName("empId")
    val empId: String?,
    @SerializedName("month")
    val month: Int?,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("status")
    val status: String?, // "COMPLETED", "PENDING", etc.
    @SerializedName("grossSalary")
    val grossSalary: Double?,
    @SerializedName("netSalary")
    val netSalary: Double?,
    @SerializedName("totalDeductions")
    val totalDeductions: Double?,
    @SerializedName("components")
    val components: List<SalaryComponent>?
)

