package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.data.model.SalaryComponent
import com.example.myapplication.viewmodel.LoginViewModel
import com.example.myapplication.viewmodel.PayrollViewModel
import com.example.myapplication.util.NavigationUtils
import java.util.Calendar
import java.util.Locale

class PayslipActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val payrollViewModel: PayrollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payslip)

        // Setup Bottom Navigation
        NavigationUtils.setupBottomNavigation(this)

        val scrollViewPayslip = findViewById<ScrollView>(R.id.scrollViewPayslip)
        val tvPayslipMonth = findViewById<TextView>(R.id.tvPayslipMonth)
        val tvEmployeeName = findViewById<TextView>(R.id.tvEmployeeName)
        val tvEmployeeId = findViewById<TextView>(R.id.tvEmployeeId)
        val tvDepartment = findViewById<TextView>(R.id.tvDepartment)
        val llEarningsContainer = findViewById<LinearLayout>(R.id.llEarningsContainer)
        val llDeductionsContainer = findViewById<LinearLayout>(R.id.llDeductionsContainer)
        val tvGrossSalary = findViewById<TextView>(R.id.tvGrossSalary)
        val tvTotalDeductions = findViewById<TextView>(R.id.tvTotalDeductions)
        val tvNetSalary = findViewById<TextView>(R.id.tvNetSalary)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnDownload = findViewById<Button>(R.id.btnDownload)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        val empSalaryId = intent.getStringExtra("EMP_SALARY_ID")
        val filterMonth = intent.getIntExtra("MONTH", -1)
        val filterYear = intent.getIntExtra("YEAR", -1)

        // Bottom Navigation Bar IDs
        val llHome = findViewById<LinearLayout>(R.id.llHome)
        val llCalendar = findViewById<LinearLayout>(R.id.llCalendar)
        val llHistory = findViewById<LinearLayout>(R.id.llHistory)

        // Set initial employee info from Session
        tvEmployeeName.text = "Employee : ${MyApplication.sessionManager.fetchUserName() ?: "N/A"}"
        tvEmployeeId.text = "Employee ID : ${MyApplication.sessionManager.fetchEmpIdSecondary() ?: "N/A"}"

        val calendar = Calendar.getInstance()
        val currentMonthName = if (filterMonth != -1) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, filterMonth - 1)
            cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        } else {
            calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        }
        val displayYear = if (filterYear != -1) filterYear else calendar.get(Calendar.YEAR)
        tvPayslipMonth.text = "$currentMonthName $displayYear Payslip"

        observePayrollData(
            scrollViewPayslip, progressBar, llEarningsContainer, llDeductionsContainer,
            tvGrossSalary, tvTotalDeductions, tvNetSalary, tvEmployeeId, tvPayslipMonth
        )

        // Fetch payroll details based on empSalaryId if provided, else fallback to month/year
        if (!empSalaryId.isNullOrEmpty()) {
            payrollViewModel.fetchPayrollDetail(empSalaryId)
        } else {
            val monthToFetch = if (filterMonth != -1) filterMonth else calendar.get(Calendar.MONTH) + 1
            val yearToFetch = if (filterYear != -1) filterYear else calendar.get(Calendar.YEAR)
            payrollViewModel.fetchPayrollDetails(monthToFetch, yearToFetch)
        }

        ivProfile.setOnClickListener {
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

        loginViewModel.logoutResult.observe(this) { result ->
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

        btnBack.setOnClickListener {
            finish()
        }

        btnDownload.setOnClickListener {
            if (!empSalaryId.isNullOrEmpty()) {
                payrollViewModel.downloadPayslip(empSalaryId)
            } else {
                val intent = Intent(this, FilterPayslipActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun observePayrollData(
        scrollView: ScrollView,
        progressBar: ProgressBar,
        llEarnings: LinearLayout,
        llDeductions: LinearLayout,
        tvGross: TextView,
        tvTotalDeductions: TextView,
        tvNet: TextView,
        tvEmpId: TextView,
        tvMonth: TextView
    ) {
        payrollViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            scrollView.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }

        // Observe Detail API response
        payrollViewModel.detailData.observe(this) { data ->
            if (data == null) return@observe
            
            tvGross.text = String.format(Locale.getDefault(), "₹ %.2f", data.grossSalary ?: 0.0)
            tvTotalDeductions.text = String.format(Locale.getDefault(), "₹ %.2f", data.totalDeductions ?: 0.0)
            tvNet.text = String.format(Locale.getDefault(), "₹ %.2f", data.netSalary ?: 0.0)

            // Dynamic Rows for Details
            while (llEarnings.childCount > 1) llEarnings.removeViewAt(1)
            while (llDeductions.childCount > 1) llDeductions.removeViewAt(1)

            // Add basic breakdown
            addManualRow(llEarnings, "Basic Salary", data.basicSalary ?: 0.0, false)
            addManualRow(llEarnings, "HRA", data.hra ?: 0.0, false)
            addManualRow(llEarnings, "Allowances", data.allowances ?: 0.0, false)
            
            addManualRow(llDeductions, "Total Deductions", data.totalDeductions ?: 0.0, true)
        }

        // Observe Old API response (api/salary)
        payrollViewModel.payrollData.observe(this) { data ->
            if (data == null) {
                // Don't show toast immediately, might be loading from detail data
                return@observe
            }
            
            Log.d("PayrollUI", "Received Payroll Data: Gross=${data.grossSalary}, Net=${data.netSalary}")
            
            data.let {
                tvGross.text = String.format(Locale.getDefault(), "₹ %.2f", it.grossSalary ?: 0.0)
                tvTotalDeductions.text = String.format(Locale.getDefault(), "₹ %.2f", it.totalDeductions ?: 0.0)
                tvNet.text = String.format(Locale.getDefault(), "₹ %.2f", it.netSalary ?: 0.0)

                if (!it.empId.isNullOrEmpty()) {
                    tvEmpId.text = "Employee ID : ${it.empId}"
                }

                // Clear previous dynamic rows (keep the header title which is at index 0)
                while (llEarnings.childCount > 1) llEarnings.removeViewAt(1)
                while (llDeductions.childCount > 1) llDeductions.removeViewAt(1)

                val components = it.components ?: emptyList()
                Log.d("PayrollUI", "Component Count: ${components.size}")

                components.forEach { component ->
                    Log.d("PayrollUI", "Adding Row: ${component.compName} - ${component.amount} (${component.compType})")
                    if (component.compType == "EARNING") {
                        addComponentRow(llEarnings, component, false)
                    } else if (component.compType == "DEDUCTION") {
                        addComponentRow(llDeductions, component, true)
                    } else {
                        // Default to earning if type is missing or different
                        addComponentRow(llEarnings, component, false)
                    }
                }
            }
        }

        payrollViewModel.downloadResult.observe(this) { result ->
            result.onSuccess { body ->
                saveAndOpenPdf(body)
            }.onFailure { t ->
                Toast.makeText(this, "Download failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }

        payrollViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addManualRow(container: LinearLayout, name: String, amount: Double, isDeduction: Boolean) {
        val component = SalaryComponent(compId = null, compName = name, amount = amount, compType = if (isDeduction) "DEDUCTION" else "EARNING")
        addComponentRow(container, component, isDeduction)
    }

    private fun saveAndOpenPdf(body: okhttp3.ResponseBody) {
        try {
            val fileName = "Payslip_${System.currentTimeMillis()}.pdf"
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
            Log.e("PayslipActivity", "Error saving PDF", e)
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addComponentRow(container: LinearLayout, component: SalaryComponent, isDeduction: Boolean) {
        val context = container.context
        val relativeLayout = RelativeLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 0) }
        }

        val nameTv = TextView(context).apply {
            text = component.compName
            setTextColor("#444444".toColorInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val amountTv = TextView(context).apply {
            text = String.format(Locale.getDefault(), "₹ %.2f", component.amount ?: 0.0)
            setTextColor(if (isDeduction) "#D81B60".toColorInt() else "#2E7D32".toColorInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.ALIGN_PARENT_END)
            layoutParams = params
        }

        relativeLayout.addView(nameTv)
        relativeLayout.addView(amountTv)
        container.addView(relativeLayout)

        // Add a small divider line
        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                setMargins(0, 8, 0, 0)
            }
            setBackgroundColor("#CCCCCC".toColorInt())
        }
        container.addView(divider)
    }
}
