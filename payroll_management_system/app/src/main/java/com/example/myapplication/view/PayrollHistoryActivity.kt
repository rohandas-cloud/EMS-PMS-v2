package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityPayrollHistoryBinding
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.view.adapter.PayrollSummaryAdapter
import com.example.myapplication.viewmodel.PayrollViewModel

class PayrollHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPayrollHistoryBinding
    private val viewModel: PayrollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayrollHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        
        // Initial fetch for April 2026
        viewModel.fetchPayrollSummary(4, 2026)
    }

    private fun setupUI() {
        binding.rvPayrollHistory.layoutManager = LinearLayoutManager(this)
        
        binding.btnBack.setOnClickListener { finish() }
        
        NavigationUtils.setupBottomNavigation(this)
    }

    private fun observeViewModel() {
        viewModel.summaryData.observe(this) { response ->
            if (response != null && response.content.isNotEmpty()) {
                binding.rvPayrollHistory.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
                binding.rvPayrollHistory.adapter = PayrollSummaryAdapter(response.content) { empSalaryId ->
                    val intent = Intent(this, PayslipActivity::class.java)
                    intent.putExtra("EMP_SALARY_ID", empSalaryId)
                    startActivity(intent)
                }
            } else {
                binding.rvPayrollHistory.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
