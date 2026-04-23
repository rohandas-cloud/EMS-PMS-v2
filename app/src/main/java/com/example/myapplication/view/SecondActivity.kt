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
import com.example.myapplication.data.model.Holiday
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.AttendanceViewModel
import com.example.myapplication.viewmodel.HolidayViewModel
import com.example.myapplication.viewmodel.LeaveViewModel
import com.example.myapplication.viewmodel.LoginViewModel
import com.example.myapplication.viewmodel.PayrollViewModel
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()
    private val leaveViewModel: LeaveViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private val payrollViewModel: PayrollViewModel by viewModels()
    private val holidayViewModel: HolidayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        NavigationUtils.setupBottomNavigation(this)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvName = findViewById<TextView>(R.id.tvName)
        val cvPayslip = findViewById<CardView>(R.id.cvPayslip)
        val cvLeave = findViewById<CardView>(R.id.cvLeave)
        val cvAttendance = findViewById<CardView>(R.id.cvAttendance)
        val cvHolidays = findViewById<CardView>(R.id.cvHolidays)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        val tvInTimeSummary = findViewById<TextView>(R.id.tvInTimeSummary)
        val tvOutTimeSummary = findViewById<TextView>(R.id.tvOutTimeSummary)
        val tvWorkingHoursSummary = findViewById<TextView>(R.id.tvWorkingHoursSummary)
        val tvLeaveBalanceSummary = findViewById<TextView>(R.id.tvLeaveBalanceSummary)
        val tvLeaveRequestSummary = findViewById<TextView>(R.id.tvLeaveRequestSummary)
        
        val tvMonthlySalaryAmount = findViewById<TextView>(R.id.tvMonthlySalaryAmount)
        val tvLastPayrollDate = findViewById<TextView>(R.id.tvLastPayrollDate)
        val tvNextHolidayCard = findViewById<TextView>(R.id.tvNextHolidayCard)
        val tvNextHolidaySummary = findViewById<TextView>(R.id.tvNextHolidaySummary)
        
        val pbSalary = findViewById<ProgressBar>(R.id.pbSalary)
        val pbSummary = findViewById<ProgressBar>(R.id.pbSummary)
        val llSalaryInfo = findViewById<View>(R.id.llSalaryInfo)
        val llSummaryContent = findViewById<View>(R.id.llSummaryContent)

        // --- GREETING AND NAME LOGIC ---
        val userName = MyApplication.sessionManager.fetchUserName() ?: "Employee"
        tvName.text = userName
        tvGreeting.text = getGreetingMessage()

        // --- ATTENDANCE LOGIC ---
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        Log.d("SecondActivity", "Dashboard empId for Attendance: $empId")
        
        if (!empId.isNullOrBlank()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            attendanceViewModel.fetchDailyAttendance(empId, today)
        }

        attendanceViewModel.dailyAttendance.observe(this) { attendance ->
            tvInTimeSummary.text = formatTime(attendance?.inTime) ?: "--:--"
            tvOutTimeSummary.text = formatTime(attendance?.outTime) ?: "--:--"
            tvWorkingHoursSummary.text = attendance?.workingHour ?: "--:--"
        }

        // Combine loading states for Summary Card
        val loadingObserver = Observer<Boolean> { 
            val isAttLoading = attendanceViewModel.isLoading.value ?: false
            val isLeaveLoading = leaveViewModel.isLoading.value ?: false
            val isHolidayLoading = holidayViewModel.isLoading.value ?: false
            
            pbSummary.visibility = if (isAttLoading || isLeaveLoading || isHolidayLoading) View.VISIBLE else View.GONE
            llSummaryContent.alpha = if (isAttLoading || isLeaveLoading || isHolidayLoading) 0.5f else 1.0f
        }
        attendanceViewModel.isLoading.observe(this, loadingObserver)
        leaveViewModel.isLoading.observe(this, loadingObserver)
        holidayViewModel.isLoading.observe(this, loadingObserver)

        attendanceViewModel.statusMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Make the "In Time" row clickable for Check-in/Out
        findViewById<View>(R.id.tvInTimeSummary).parent.let { 
            (it as? View)?.setOnClickListener {
                attendanceViewModel.markAttendance()
            }
        }

        // --- LEAVE LOGIC ---
        leaveViewModel.fetchLeaveBalance()
        leaveViewModel.leaveBalances.observe(this) { balance ->
            if (balance != null) {
                tvLeaveBalanceSummary.text = "${balance.casualLeave ?: 0} / ${balance.totalLeave ?: 0}"
            }
        }

        leaveViewModel.fetchLeaveHistory()
        leaveViewModel.leaveHistory.observe(this) { history ->
            val pendingCount = history?.count { it.status?.contains("Pending", ignoreCase = true) == true } ?: 0
            tvLeaveRequestSummary.text = if (pendingCount > 0) "$pendingCount Pending" else "No Pending Request"
        }

        // --- SALARY LOGIC ---
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        payrollViewModel.fetchPayrollFromSession(currentMonth, currentYear)

        payrollViewModel.payrollData.observe(this) { data ->
            if (data != null) {
                tvMonthlySalaryAmount.text = String.format(Locale.getDefault(), getString(R.string.currency_format), data.netSalary ?: 0.0)
                tvLastPayrollDate.text = getString(R.string.last_payroll_label, data.month ?: "--", data.year ?: "----")
            }
        }

        payrollViewModel.isLoading.observe(this) { isLoading ->
            pbSalary.visibility = if (isLoading) View.VISIBLE else View.GONE
            llSalaryInfo.alpha = if (isLoading) 0.5f else 1.0f
        }

        // --- HOLIDAY LOGIC ---
        holidayViewModel.loadMyHolidays()
        holidayViewModel.holidays.observe(this) { holidays ->
            if (!holidays.isNullOrEmpty()) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val nextHoliday = holidays
                    .filter { it.date >= todayStr }
                    .minByOrNull { it.date }
                
                nextHoliday?.let {
                    val displayDate = try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                        SimpleDateFormat("dd MMM", Locale.getDefault()).format(date!!)
                    } catch (e: Exception) {
                        it.date
                    }
                    tvNextHolidayCard.text = "Next: $displayDate - ${it.name}"
                    tvNextHolidaySummary.text = displayDate
                } ?: run {
                    tvNextHolidayCard.text = "No upcoming holidays"
                }
            }
        }

        ivProfile.setOnClickListener {
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

        cvPayslip.setOnClickListener { startActivity(Intent(this, PayslipActivity::class.java)) }
        cvLeave.setOnClickListener { startActivity(Intent(this, LeaveRequestActivity::class.java)) }
        cvAttendance.setOnClickListener { startActivity(Intent(this, Calender::class.java)) }
        cvHolidays.setOnClickListener { startActivity(Intent(this, HolidayActivity::class.java)) }
    }

    private fun formatTime(timeStr: String?): String? {
        if (timeStr == null) return null
        return try {
            // Handles "16:22:02.401492159" or "16:22:02"
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                "${parts[0]}:${parts[1]}"
            } else {
                timeStr
            }
        } catch (e: Exception) {
            timeStr
        }
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..15 -> "Good Afternoon"
            in 16..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
}