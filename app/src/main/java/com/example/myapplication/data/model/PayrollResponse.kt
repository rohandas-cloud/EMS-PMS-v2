package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class PayrollResponse(
    @SerializedName("empId")
    val empId: String?,
    @SerializedName("grossSalary")
    val grossSalary: Double?,
    @SerializedName("netSalary")
    val netSalary: Double?,
    @SerializedName("totalDeductions")
    val totalDeductions: Double?,
    @SerializedName("components")
    val components: List<SalaryComponent>?
)

data class SalaryComponent(
    @SerializedName("compId")
    val compId: String?,
    @SerializedName("compName")
    val compName: String?,
    @SerializedName("compType")
    val compType: String?, // "EARNING" or "DEDUCTION"
    @SerializedName("amount")
    val amount: Double?
)
