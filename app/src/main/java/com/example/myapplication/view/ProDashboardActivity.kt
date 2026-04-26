package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A professional dashboard activity with multiple design options.
 * You can switch between designs by changing the layout resource in onCreate.
 */
class ProDashboardActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()
    private val leaveViewModel: LeaveViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private val payrollViewModel: PayrollViewModel by viewModels()
    private val holidayViewModel: HolidayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- CHOOSE DESIGN HERE ---
        // Option 1: R.layout.activity_pro_dashboard_v1 (Minimalist & Clean)
        // Option 2: R.layout.activity_pro_dashboard_v2 (Modern Professional)
        // Option 3: R.layout.activity_pro_dashboard_v3 (Productivity Focused)
        // Option 4: R.layout.activity_pro_dashboard_v4 (Elite Management - Ultra Minimalist)
        // Option 5: R.layout.activity_pro_dashboard_v5 (Bento Style - Reference Design)
        setContentView(R.layout.activity_pro_dashboard_v5)

        // Setup common UI elements (matching IDs across versions)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvName = findViewById<TextView>(R.id.tvName)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        
        val cvPayslip = findViewById<CardView>(R.id.cvPayslip)
        val cvLeave = findViewById<CardView>(R.id.cvLeave)
        val cvAttendance = findViewById<CardView>(R.id.cvAttendance)
        val cvHolidays = findViewById<CardView>(R.id.cvHolidays)

        val tvInTimeSummary = findViewById<TextView>(R.id.tvInTimeSummary)
        val tvMonthlySalaryAmount = findViewById<TextView>(R.id.tvMonthlySalaryAmount)
        val tvLastPayrollDate = findViewById<TextView>(R.id.tvLastPayrollDate)
        val tvNextHolidayCard = findViewById<TextView>(R.id.tvNextHolidayCard)
        
        val pbSalary = findViewById<ProgressBar>(R.id.pbSalary)
        val llSalaryInfo = findViewById<View>(R.id.llSalaryInfo)

        // --- GREETING AND NAME LOGIC ---
        val userName = MyApplication.sessionManager.fetchUserName() ?: "Employee"
        tvName.text = userName
        tvGreeting.text = getGreetingMessage()

        // --- DATA FETCHING ---
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        if (!empId.isNullOrBlank()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            attendanceViewModel.fetchDailyAttendance(empId, today)
        }

        attendanceViewModel.dailyAttendance.observe(this) { attendance ->
            tvInTimeSummary?.text = formatTime(attendance?.inTime) ?: "--:--"
        }

        leaveViewModel.fetchLeaveBalance()
        leaveViewModel.leaveBalances.observe(this) { balance ->
            if (balance != null) {
                findViewById<TextView>(R.id.tvLeaveBalanceSummary)?.text = "${balance.casualLeave ?: 0} / ${balance.totalLeave ?: 0}"
            }
        }

        val calendar = Calendar.getInstance()
        payrollViewModel.fetchPayrollFromSession(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
        payrollViewModel.payrollData.observe(this) { data ->
            if (data != null) {
                tvMonthlySalaryAmount?.text = String.format(Locale.getDefault(), getString(R.string.currency_format), data.netSalary ?: 0.0)
                tvLastPayrollDate?.text = "Paid on ${data.month ?: "--"} ${data.year ?: "----"}"
            }
        }

        holidayViewModel.loadMyHolidays()
        holidayViewModel.holidays.observe(this) { holidays ->
            if (!holidays.isNullOrEmpty()) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val nextHoliday = holidays.filter { it.date >= todayStr }.minByOrNull { it.date }
                nextHoliday?.let {
                    tvNextHolidayCard?.text = "Next: ${it.name}"
                }
            }
        }

        // --- NAVIGATION ---
        cvPayslip?.setOnClickListener { startActivity(Intent(this, PayslipActivity::class.java)) }
        cvLeave?.setOnClickListener { startActivity(Intent(this, LeaveRequestActivity::class.java)) }
        cvAttendance?.setOnClickListener { startActivity(Intent(this, Calender::class.java)) }
        cvHolidays?.setOnClickListener { startActivity(Intent(this, HolidayActivity::class.java)) }

        ivProfile?.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("Logout")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Logout") viewModel.logout()
                true
            }
            popup.show()
        }

        viewModel.logoutResult.observe(this) { result ->
            if (result.isSuccess) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }

    private fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..15 -> "Good Afternoon"
            in 16..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    private fun formatTime(timeStr: String?): String? {
        if (timeStr == null) return null
        return try {
            val parts = timeStr.split(":")
            if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timeStr
        } catch (e: Exception) { timeStr }
    }
}
