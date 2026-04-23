package com.example.myapplication.view

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.data.api.RetrofitClient
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch

/**
 * DEBUG COMPONENT: Used to inspect raw database data.
 * Can be deleted at any time without affecting the core app.
 */
class DebugDataActivity : AppCompatActivity() {

    private lateinit var tvRawOutput: TextView
    private lateinit var tvSessionInfo: TextView
    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_data)

        tvRawOutput = findViewById(R.id.tvRawOutput)
        tvSessionInfo = findViewById(R.id.tvSessionInfo)

        findViewById<Button>(R.id.btnFetchAttendance).setOnClickListener { fetchAttendance() }
        findViewById<Button>(R.id.btnFetchPayroll).setOnClickListener { fetchPayroll() }
        findViewById<Button>(R.id.btnFetchHolidays).setOnClickListener { fetchHolidays() }
        findViewById<Button>(R.id.btnCloseDebug).setOnClickListener { finish() }

        updateSessionInfo()
    }

    private fun updateSessionInfo() {
        val pmsId = MyApplication.sessionManager.fetchEmpIdPms()
        val emsId = MyApplication.sessionManager.fetchEmpIdEms()
        val email = MyApplication.sessionManager.fetchUserEmail()
        
        tvSessionInfo.text = """
            SESSION METADATA:
            PMS EmpId: $pmsId
            EMS EmpId: $emsId
            Email: $email
        """.trimIndent()
    }

    private fun fetchAttendance() {
        val empId = MyApplication.sessionManager.fetchEmpIdEms() ?: return
        tvRawOutput.text = "Fetching raw attendance for EMS ID: $empId..."
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.emsApi.getAttendanceHistory(empId)
                if (response.isSuccessful) {
                    val body = response.body()
                    tvRawOutput.text = "SUCCESS (EMS):\n${gson.toJson(body)}"
                } else {
                    tvRawOutput.text = "ERROR ${response.code()}:\n${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                tvRawOutput.text = "EXCEPTION:\n${e.message}"
            }
        }
    }

    private fun fetchPayroll() {
        val empId = MyApplication.sessionManager.fetchEmpIdPms() ?: return
        tvRawOutput.text = "Fetching raw payroll summary for PMS ID: $empId..."
        
        lifecycleScope.launch {
            try {
                // Fetching for a generic month/year for debugging
                val response = RetrofitClient.pmsApi.getPayrollByMonthYear(empId, 4, 2026)
                if (response.isSuccessful) {
                    tvRawOutput.text = "SUCCESS (PMS):\n${gson.toJson(response.body())}"
                } else {
                    tvRawOutput.text = "ERROR ${response.code()}:\n${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                tvRawOutput.text = "EXCEPTION:\n${e.message}"
            }
        }
    }

    private fun fetchHolidays() {
        tvRawOutput.text = "Fetching raw holidays list..."
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.pmsApi.getHolidays()
                if (response.isSuccessful) {
                    tvRawOutput.text = "SUCCESS:\n${gson.toJson(response.body())}"
                } else {
                    tvRawOutput.text = "ERROR ${response.code()}:\n${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                tvRawOutput.text = "EXCEPTION:\n${e.message}"
            }
        }
    }
}
