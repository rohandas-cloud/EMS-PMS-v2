package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.LeaveViewModel
import com.example.myapplication.viewmodel.LoginViewModel

class SecondActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()
    private val leaveViewModel: LeaveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Setup Bottom Navigation
        NavigationUtils.setupBottomNavigation(this)

        val tvName = findViewById<TextView>(R.id.tvName)
        val cvPayslip = findViewById<CardView>(R.id.cvPayslip)
        val cvLeave = findViewById<CardView>(R.id.cvLeave)
        val cvAttendance = findViewById<CardView>(R.id.cvAttendance)
        val cvHolidays = findViewById<CardView>(R.id.cvHolidays)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        // Attendance Summary UI
        val tvInTimeSummary = findViewById<TextView>(R.id.tvInTimeSummary)
        val tvOutTimeSummary = findViewById<TextView>(R.id.tvOutTimeSummary)
        val tvWorkingHoursSummary = findViewById<TextView>(R.id.tvWorkingHoursSummary)
        val tvLeaveBalanceSummary = findViewById<TextView>(R.id.tvLeaveBalanceSummary)
        val tvLeaveRequestSummary = findViewById<TextView>(R.id.tvLeaveRequestSummary)

        // Get User Name from Session Manager or Intent
        val userName = MyApplication.sessionManager.fetchUserName() ?: intent.getStringExtra("USER_NAME") ?: "User"
        tvName.text = userName

        // --- Leave Balance Logic (Summary) ---
        leaveViewModel.fetchLeaveBalance()
        leaveViewModel.leaveBalances.observe(this) { balance ->
            Log.d("SecondActivity", "========== LEAVE BALANCE DATA RECEIVED ==========")
            if (balance != null) {
                val totalLeave = balance.totalLeave ?: 0
                val casualLeave = balance.casualLeave ?: 0
                val sickLeave = balance.sickLeave ?: 0
                val earnedLeave = balance.earnedLeave ?: 0
                
                Log.d("SecondActivity", "Leave Balance Object: $balance")
                Log.d("SecondActivity", "  casualLeave: $casualLeave")
                Log.d("SecondActivity", "  sickLeave: $sickLeave")
                Log.d("SecondActivity", "  earnedLeave: $earnedLeave")
                Log.d("SecondActivity", "  totalLeave: $totalLeave")
                Log.d("SecondActivity", "  message: ${balance.message}")
                
                val balanceText = "$casualLeave / $totalLeave"
                Log.d("SecondActivity", "UI Binding - tvLeaveBalanceSummary: '$balanceText'")
                tvLeaveBalanceSummary.text = balanceText
            } else {
                Log.d("SecondActivity", "Leave balance is null")
                tvLeaveBalanceSummary.text = "0 / 0"
            }
            Log.d("SecondActivity", "====================================================")
        }

        leaveViewModel.errorMessage.observe(this) { error ->
            error?.let {
                Log.e("SecondActivity", "Leave Balance Error: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // --- Leave History (Summary) ---
        leaveViewModel.fetchLeaveHistory()
        leaveViewModel.leaveHistory.observe(this) { history ->
            Log.d("SecondActivity", "========== LEAVE HISTORY DATA RECEIVED ==========")
            Log.d("SecondActivity", "History list: $history")
            
            val pendingCount = history?.count { it.status?.equals("Pending", ignoreCase = true) == true } ?: 0
            Log.d("SecondActivity", "Pending leave count: $pendingCount")
            
            if (pendingCount > 0) {
                val pendingText = "$pendingCount Pending"
                Log.d("SecondActivity", "UI Binding - tvLeaveRequestSummary: '$pendingText'")
                tvLeaveRequestSummary.text = pendingText
            } else {
                Log.d("SecondActivity", "UI Binding - tvLeaveRequestSummary: 'No Pending Request'")
                tvLeaveRequestSummary.text = "No Pending Request"
            }
            Log.d("SecondActivity", "====================================================")
        }

        // Note: Leave history uses the same errorMessage, so it's already observed above

        ivProfile.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("Logout")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Logout") {
                    viewModel.logout()
                }
                true
            }
            popup.show()
        }

        viewModel.logoutResult.observe(this) { result ->
            result.onSuccess {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { t ->
                Toast.makeText(this, "Logout failed: ${t.message}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // --- Card Click Listeners ---
        cvPayslip.setOnClickListener {
            startActivity(Intent(this, PayslipActivity::class.java))
        }

        cvLeave.setOnClickListener {
            val intent = Intent(this, LeaveRequestActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvAttendance.setOnClickListener {
            val intent = Intent(this, Calender::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvHolidays.setOnClickListener {
            startActivity(Intent(this, HolidayActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }
}