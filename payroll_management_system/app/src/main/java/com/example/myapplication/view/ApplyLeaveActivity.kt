package com.example.myapplication.view

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.data.model.LeaveApplyRequest
import com.example.myapplication.data.model.LeaveBalanceItem
import com.example.myapplication.data.model.LeaveType
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.LeaveViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class ApplyLeaveActivity : AppCompatActivity() {

    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var etNoOfDays: EditText
    private lateinit var etReason: EditText
    private lateinit var autoCompleteLeaveType: AutoCompleteTextView
    private lateinit var autoCompleteDayType: AutoCompleteTextView
    private lateinit var layoutDayType: View
    private lateinit var tvDayTypeTitle: View
    private lateinit var rgHalfDay: RadioGroup
    private lateinit var rbFirstHalf: RadioButton
    private lateinit var rbSecondHalf: RadioButton
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar

    private val viewModel: LeaveViewModel by viewModels()

    private var selectedType: LeaveType? = null
    private var allLeaveTypes: List<LeaveType> = emptyList()
    private var leaveBalance: com.example.myapplication.data.model.LeaveBalanceResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_leave)

        initViews()
        setupDatePickers()
        setupObservers()

        viewModel.fetchLeaveBalance()
        viewModel.fetchLeaveTypes()

        NavigationUtils.setupBottomNavigation(this)

        btnSubmit.setOnClickListener { validateAndApply() }
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun initViews() {
        etFromDate = findViewById(R.id.etFromDate)
        etToDate = findViewById(R.id.etToDate)
        etNoOfDays = findViewById(R.id.etNoOfDays)
        etReason = findViewById(R.id.etReason)

        autoCompleteLeaveType = findViewById(R.id.autoCompleteLeaveType)
        autoCompleteDayType = findViewById(R.id.autoCompleteDayType)

        layoutDayType = findViewById(R.id.layoutDayType)
        tvDayTypeTitle = findViewById(R.id.tvDayTypeTitle)

        rgHalfDay = findViewById(R.id.rgHalfDay)
        rbFirstHalf = findViewById(R.id.rbFirstHalf)
        rbSecondHalf = findViewById(R.id.rbSecondHalf)

        btnSubmit = findViewById(R.id.btnApplyLeave)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupObservers() {

        viewModel.leaveTypes.observe(this) { types ->
            Log.d("ApplyLeaveActivity", "========== LEAVE TYPES OBSERVED ==========")
            allLeaveTypes = types ?: emptyList()
            Log.d("ApplyLeaveActivity", "Leave types count: ${allLeaveTypes.size}")
            Log.d("ApplyLeaveActivity", "Leave types: $allLeaveTypes")
            updateDropdown()
            Log.d("ApplyLeaveActivity", "====================================================")
        }

        viewModel.leaveBalances.observe(this) { balance ->
            Log.d("ApplyLeaveActivity", "========== LEAVE BALANCE OBSERVED ==========")
            leaveBalance = balance
            Log.d("ApplyLeaveActivity", "Balance object: $balance")
            if (balance != null) {
                Log.d("ApplyLeaveActivity", "  casualLeave: ${balance.casualLeave}")
                Log.d("ApplyLeaveActivity", "  sickLeave: ${balance.sickLeave}")
                Log.d("ApplyLeaveActivity", "  earnedLeave: ${balance.earnedLeave}")
                Log.d("ApplyLeaveActivity", "  totalLeave: ${balance.totalLeave}")
            }
            updateDropdown()
            Log.d("ApplyLeaveActivity", "====================================================")
        }

        viewModel.applyResult.observe(this) { result ->
            val (success, message) = result
            Log.d("ApplyLeaveActivity", "========== APPLY LEAVE RESULT ==========")
            Log.d("ApplyLeaveActivity", "Success: $success")
            Log.d("ApplyLeaveActivity", "Message: $message")
            Log.d("ApplyLeaveActivity", "====================================================")
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            if (success) finish()
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnSubmit.isEnabled = !loading
        }

        viewModel.error.observe(this) { msg ->
            msg?.let { 
                Log.e("ApplyLeaveActivity", "Error: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show() 
            }
        }
    }

    // ✅ FIXED DROPDOWN
    private fun updateDropdown() {
        Log.d("ApplyLeaveActivity", "========== UPDATE DROPDOWN ==========")
        Log.d("ApplyLeaveActivity", "allLeaveTypes.isEmpty(): ${allLeaveTypes.isEmpty()}")
        Log.d("ApplyLeaveActivity", "allLeaveTypes: $allLeaveTypes")
        Log.d("ApplyLeaveActivity", "leaveBalance: $leaveBalance")
        
        if (allLeaveTypes.isEmpty()) {
            Log.d("ApplyLeaveActivity", "Skipping dropdown update - no leave types available")
            return
        }

        val displayList = allLeaveTypes.map { type ->
            val remaining = leaveBalance?.casualLeave ?: 0
            val displayText = "${type.type} ($remaining left)"
            Log.d("ApplyLeaveActivity", "  Mapping type: ${type.type} -> $displayText")
            displayText
        }

        Log.d("ApplyLeaveActivity", "Display list: $displayList")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            displayList
        )

        autoCompleteLeaveType.setAdapter(adapter)
        Log.d("ApplyLeaveActivity", "Adapter set on autoCompleteLeaveType")

        autoCompleteLeaveType.setOnItemClickListener { _, _, position, _ ->
            Log.d("ApplyLeaveActivity", "Dropdown item clicked at position: $position")
            if (position in allLeaveTypes.indices) {
                selectedType = allLeaveTypes[position]
                Log.d("ApplyLeaveActivity", "Selected type: ${selectedType?.type}")
            }
        }
        Log.d("ApplyLeaveActivity", "====================================================")
    }

    private fun setupDatePickers() {

        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()

        val listener = View.OnClickListener {
            if (!dateRangePicker.isAdded) {
                dateRangePicker.show(supportFragmentManager, "DATE_PICKER")
            }
        }

        etFromDate.setOnClickListener(listener)
        etToDate.setOnClickListener(listener)

        dateRangePicker.addOnPositiveButtonClickListener { selection ->

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            etFromDate.setText(sdf.format(Date(selection.first)))
            etToDate.setText(sdf.format(Date(selection.second)))

            val days =
                ((selection.second - selection.first) / (1000 * 60 * 60 * 24)).toInt() + 1

            etNoOfDays.setText(days.toString())

            updateDayTypeVisibility(days)
        }
    }

    private fun updateDayTypeVisibility(days: Int) {

        val options = if (days == 1)
            listOf("Full Day", "Half Day")
        else
            listOf("Full Day")

        autoCompleteDayType.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        )

        autoCompleteDayType.setText("Full Day", false)

        layoutDayType.visibility = View.VISIBLE
        tvDayTypeTitle.visibility = View.VISIBLE
        rgHalfDay.visibility = View.GONE

        autoCompleteDayType.setOnItemClickListener { _, _, _, _ ->
            rgHalfDay.visibility =
                if (autoCompleteDayType.text.toString() == "Half Day")
                    View.VISIBLE
                else
                    View.GONE
        }
    }

    private fun validateAndApply() {

        val type = selectedType ?: run {
            Toast.makeText(this, "Select Leave Type", Toast.LENGTH_SHORT).show()
            return
        }

        val startDate = etFromDate.text.toString()
        val endDate = etToDate.text.toString()
        val reason = etReason.text.toString()
        val days = etNoOfDays.text.toString().toDoubleOrNull() ?: 0.0

        if (startDate.isBlank() || endDate.isBlank() || reason.isBlank()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (days > type.maxConsecutiveDays) {
            Toast.makeText(
                this,
                "Max allowed for ${type.type} is ${type.maxConsecutiveDays} days",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val dayParam =
            if (autoCompleteDayType.text.toString() == "Half Day")
                if (rbFirstHalf.isChecked) "FIRST_HALF" else "SECOND_HALF"
            else "FULL"

        val empId =
            com.example.myapplication.MyApplication.sessionManager.fetchEmpIdEms().orEmpty()

        if (empId.isEmpty()) {
            Toast.makeText(this, "Employee ID not found. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LeaveApplyRequest(
            empId = empId,
            leaveType = type.type,
            reason = reason,
            noOfDays = if (dayParam == "FULL") days else 0.5,
            startDate = startDate,
            endDate = endDate,
            leaveDay = dayParam
        )
        
        android.util.Log.d("ApplyLeaveActivity", "Submitting leave request: $request")
        viewModel.applyLeave(request)
    }
}