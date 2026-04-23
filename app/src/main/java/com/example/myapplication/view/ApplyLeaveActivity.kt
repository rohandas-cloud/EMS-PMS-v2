package com.example.myapplication.view

import android.os.Bundle
import android.util.Log
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
    private var balanceItems: List<LeaveBalanceItem> = emptyList()

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
            allLeaveTypes = types ?: emptyList()
            updateDropdown()
        }

        viewModel.leaveBalancesList.observe(this) { items ->
            balanceItems = items ?: emptyList()
            updateDropdown()
        }

        viewModel.applyResult.observe(this) { result ->
            val (success, message) = result
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            if (success) finish()
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnSubmit.isEnabled = !loading
        }

        viewModel.error.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun updateDropdown() {
        if (allLeaveTypes.isEmpty()) return

        val displayList = allLeaveTypes.map { type ->
            val balanceItem = balanceItems.find { 
                it.leaveType?.contains(type.type, ignoreCase = true) == true 
            }
            val remaining = balanceItem?.remainingLeaves?.toInt() ?: 0
            getString(R.string.leave_type_remaining, type.type, remaining)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, displayList)
        autoCompleteLeaveType.setAdapter(adapter)
        
        autoCompleteLeaveType.setOnClickListener {
            if (allLeaveTypes.isEmpty()) viewModel.fetchLeaveTypes()
            autoCompleteLeaveType.showDropDown()
        }

        autoCompleteLeaveType.setOnItemClickListener { _, _, position, _ ->
            if (position in allLeaveTypes.indices) {
                selectedType = allLeaveTypes[position]
                autoCompleteLeaveType.error = null
            }
        }
    }

    private fun setupDatePickers() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.select_date_range)).build()

        val listener = View.OnClickListener {
            if (!dateRangePicker.isAdded) dateRangePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        etFromDate.setOnClickListener(listener)
        etToDate.setOnClickListener(listener)

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etFromDate.setText(sdf.format(Date(selection.first)))
            etToDate.setText(sdf.format(Date(selection.second)))
            val days = ((selection.second - selection.first) / (1000 * 60 * 60 * 24)).toInt() + 1
            etNoOfDays.setText(days.toString())
            updateDayTypeVisibility(days)
        }
    }

    private fun updateDayTypeVisibility(days: Int) {
        val options = if (days == 1) listOf(getString(R.string.full_day), getString(R.string.half_day)) else listOf(getString(R.string.full_day))
        autoCompleteDayType.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options))
        autoCompleteDayType.setText(getString(R.string.full_day), false)
        layoutDayType.visibility = View.VISIBLE
        tvDayTypeTitle.visibility = View.VISIBLE
        rgHalfDay.visibility = View.GONE

        autoCompleteDayType.setOnItemClickListener { _, _, _, _ ->
            rgHalfDay.visibility = if (autoCompleteDayType.text.toString() == getString(R.string.half_day)) View.VISIBLE else View.GONE
        }
    }

    private fun validateAndApply() {
        val type = selectedType ?: run {
            Toast.makeText(this, getString(R.string.select_leave_type), Toast.LENGTH_SHORT).show()
            return
        }

        val startDate = etFromDate.text.toString()
        val endDate = etToDate.text.toString()
        val reason = etReason.text.toString()
        val days = etNoOfDays.text.toString().toDoubleOrNull() ?: 0.0

        if (startDate.isBlank() || endDate.isBlank() || reason.isBlank()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val dayParam = if (autoCompleteDayType.text.toString() == getString(R.string.half_day))
                if (rbFirstHalf.isChecked) "FIRST_HALF" else "SECOND_HALF"
            else "FULL"

        val empId = com.example.myapplication.MyApplication.sessionManager.fetchEmpIdEms().orEmpty()
        
        // Find the specific empLeaveId for this leave type from the balance items
        val balanceItem = balanceItems.find { 
            it.leaveType?.contains(type.type, ignoreCase = true) == true 
        }
        val targetEmpId = balanceItem?.empLeaveId ?: empId

        if (targetEmpId.isEmpty()) {
            Toast.makeText(this, getString(R.string.emp_id_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val request = LeaveApplyRequest(
            empLeaveId = targetEmpId,
            leaveType = type.type,
            description = reason,
            noOfDays = if (dayParam == "FULL") days else 0.5,
            startDate = startDate,
            endDate = endDate,
            leaveDay = dayParam
        )
        
        Log.d("ApplyLeaveActivity", "Submitting request with ID: $targetEmpId for type: ${type.type}")
        viewModel.applyLeave(request)
    }
}