package com.example.myapplication.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.ProgressBar
import android.widget.NumberPicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.viewmodel.LoginViewModel
import com.example.myapplication.viewmodel.PayrollViewModel
import com.example.myapplication.util.NavigationUtils
import java.util.Calendar
import java.util.Locale

class FilterPayslipActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val payrollViewModel: PayrollViewModel by viewModels()
    
    private lateinit var tvSelectedMonth: TextView
    private lateinit var tvSelectedYear: TextView
    private lateinit var tvEmployeeNameCard: TextView
    private lateinit var tvEmployeeIdCard: TextView
    private lateinit var btnApply: Button
    private lateinit var btnDownload: Button
    private lateinit var btnView: Button
    private lateinit var progressBar: ProgressBar

    private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private var currentMonthIdx = 3 // April
    private var currentYear = 2026

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_payslip)

        // Initialize UI components first
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        tvSelectedYear = findViewById(R.id.tvSelectedYear)
        tvEmployeeNameCard = findViewById(R.id.tvEmployeeNameCard)
        tvEmployeeIdCard = findViewById(R.id.tvEmployeeIdCard)
        btnApply = findViewById(R.id.btnApply)
        btnDownload = findViewById(R.id.btnDownload)
        btnView = findViewById(R.id.btnView)
        progressBar = findViewById(R.id.progressBar)

        // Setup Navigation
        NavigationUtils.setupBottomNavigation(this)

        // Set Default Values
        tvSelectedMonth.text = months[currentMonthIdx]
        tvSelectedYear.text = currentYear.toString()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.llMonthSelector).setOnClickListener {
            showMonthPicker()
        }

        findViewById<LinearLayout>(R.id.llYearSelector).setOnClickListener {
            showYearPicker()
        }

        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("Logout")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Logout") {
                    loginViewModel.logout()
                }
                true
            }
            popup.show()
        }

        // Apply Button Logic
        btnApply.setOnClickListener {
            val month = currentMonthIdx + 1
            val year = currentYear
            payrollViewModel.fetchPayrollDetails(month, year)
        }

        // View Button Logic
        btnView.setOnClickListener {
            val intent = Intent(this, PayslipActivity::class.java)
            intent.putExtra("MONTH", currentMonthIdx + 1)
            intent.putExtra("YEAR", currentYear)
            startActivity(intent)
        }

        // Download Button Logic
        btnDownload.setOnClickListener {
            val month = currentMonthIdx + 1
            val year = currentYear
            payrollViewModel.downloadPayslipByMonth(month, year)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        payrollViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        payrollViewModel.payrollData.observe(this) { data ->
            if (data != null && (data.grossSalary ?: 0.0) > 0) {
                Toast.makeText(this, "Payslip found for ${months[currentMonthIdx]} $currentYear", Toast.LENGTH_SHORT).show()
                btnDownload.isEnabled = true
                btnView.isEnabled = true
            } else {
                // If data is null, the error message from VM will be shown via its observer
                btnDownload.isEnabled = false
                btnView.isEnabled = false
            }
        }

        payrollViewModel.downloadByMonthResult.observe(this) { result ->
            result.onSuccess { body ->
                saveAndOpenPdf(body)
            }.onFailure { t ->
                Toast.makeText(this, "Download failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }

        payrollViewModel.errorMessage.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        loginViewModel.logoutResult.observe(this) { result ->
            result.onSuccess {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun saveAndOpenPdf(body: okhttp3.ResponseBody) {
        try {
            val fileName = "Payslip_${months[currentMonthIdx]}_$currentYear.pdf"
            val file = java.io.File(getExternalFilesDir(null), fileName)
            val inputStream = body.byteStream()
            val outputStream = java.io.FileOutputStream(file)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open Payslip"))
            
        } catch (e: Exception) {
            Log.e("FilterPayslipActivity", "Error saving PDF", e)
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMonthPicker() {
        val picker = NumberPicker(this)
        picker.minValue = 0
        picker.maxValue = 11
        picker.displayedValues = months
        picker.value = currentMonthIdx

        AlertDialog.Builder(this)
            .setTitle("Select Month")
            .setView(picker)
            .setPositiveButton("OK") { _, _ ->
                currentMonthIdx = picker.value
                tvSelectedMonth.text = months[currentMonthIdx]
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showYearPicker() {
        val picker = NumberPicker(this)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        picker.minValue = year - 5
        picker.maxValue = year + 5
        picker.value = currentYear

        AlertDialog.Builder(this)
            .setTitle("Select Year")
            .setView(picker)
            .setPositiveButton("OK") { _, _ ->
                currentYear = picker.value
                tvSelectedYear.text = currentYear.toString()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
