package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.LeaveViewModel
import com.example.myapplication.viewmodel.LoginViewModel
import android.util.Log

class LeaveRequestActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val leaveViewModel: LeaveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_request)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnApplyLeave = findViewById<Button>(R.id.btnApplyLeave)
        val rvLeaveBalances = findViewById<RecyclerView>(R.id.rvLeaveBalances)
        val pbBalances = findViewById<ProgressBar>(R.id.pbBalances)
        val rvLeaveHistory = findViewById<RecyclerView>(R.id.rvLeaveHistory)
        val pbHistory = findViewById<ProgressBar>(R.id.pbHistory)

        rvLeaveBalances.layoutManager = LinearLayoutManager(this)
        rvLeaveHistory.layoutManager = LinearLayoutManager(this)

        // Set Global Navigation
        NavigationUtils.setupBottomNavigation(this)

        btnBack.setOnClickListener { finish() }

        btnApplyLeave.setOnClickListener {
            startActivity(Intent(this, ApplyLeaveActivity::class.java))
        }

        // Observe Data
        leaveViewModel.leaveBalances.observe(this) { balance ->
            balance?.let {
                Log.d("LeaveRequestActivity", "========== LEAVE BALANCE UI BINDING ==========")
                Log.d("LeaveRequestActivity", "Balance object: $it")
                Log.d("LeaveRequestActivity", "  casualLeave: ${it.casualLeave}")
                Log.d("LeaveRequestActivity", "  sickLeave: ${it.sickLeave}")
                Log.d("LeaveRequestActivity", "  earnedLeave: ${it.earnedLeave}")
                Log.d("LeaveRequestActivity", "  totalLeave: ${it.totalLeave}")
                // Convert single balance object to list with 3 items (Casual, Sick, Earned)
                rvLeaveBalances.adapter = LeaveBalanceAdapter(listOf(it, it, it))
                Log.d("LeaveRequestActivity", "Adapter set with 3 balance items")
                Log.d("LeaveRequestActivity", "====================================================")
            }
        }

        leaveViewModel.isLoading.observe(this) { isLoading ->
            pbBalances.visibility = if (isLoading) View.VISIBLE else View.GONE
            pbHistory.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        leaveViewModel.leaveHistory.observe(this) { history ->
            history?.let {
                Log.d("LeaveRequestActivity", "========== LEAVE HISTORY UI BINDING ==========")
                Log.d("LeaveRequestActivity", "History count: ${it.size}")
                Log.d("LeaveRequestActivity", "History list: $it")
                rvLeaveHistory.adapter = LeaveHistoryAdapter(it)
                Log.d("LeaveRequestActivity", "Adapter set with ${it.size} history items")
                Log.d("LeaveRequestActivity", "====================================================")
            }
        }
        
        leaveViewModel.errorMessage.observe(this) { error ->
            error?.let {
                android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show()
            }
        }

        leaveViewModel.fetchLeaveBalance()
        leaveViewModel.fetchLeaveHistory()
    }
}
