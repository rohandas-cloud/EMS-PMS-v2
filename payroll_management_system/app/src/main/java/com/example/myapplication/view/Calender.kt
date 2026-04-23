package com.example.myapplication.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.AttendanceViewModel
import com.example.myapplication.viewmodel.CalendarViewModel

class Calender : AppCompatActivity() {
    
    private val viewModel: CalendarViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        NavigationUtils.setupBottomNavigation(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rvCalendar)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvMonthYear = findViewById<TextView>(R.id.tvMonthYear)

        // Summary Views
        val tvPresentCount = findViewById<TextView>(R.id.tvPresentCount)
        val tvAbsentCount = findViewById<TextView>(R.id.tvAbsentCount)
        val tvWeekendCount = findViewById<TextView>(R.id.tvWeekendCount)
        val tvAverageTime = findViewById<TextView>(R.id.tvAverageTime)

        btnBack.setOnClickListener { finish() }

        // attendanceViewModel.fetchTodayAttendance()

        recyclerView.layoutManager = GridLayoutManager(this, 7)

        // Observers
        viewModel.currentMonthName.observe(this) { tvMonthYear.text = it }
        viewModel.presentCount.observe(this) { count ->
            tvPresentCount.text = getString(R.string.days_count_format, count)
        }
        viewModel.absentCount.observe(this) { count ->
            tvAbsentCount.text = getString(R.string.days_count_format, count)
        }
        viewModel.weekendCount.observe(this) { count ->
            tvWeekendCount.text = getString(R.string.days_count_format, count)
        }
        viewModel.averageWorkingHours.observe(this) { tvAverageTime.text = it }

        viewModel.calendarDays.observe(this) { daysList ->
            recyclerView.adapter = CalendarAdapter(daysList) { day ->
                viewModel.onDateSelected(day)
            }
        }

        viewModel.selectedAttendance.observe(this) { record ->
            if (record != null) {
                // Show dialog with attendance details
                showAttendanceDialog(record)
            } else {
                // Show message that no attendance record found
                Toast.makeText(this, "No attendance record found for this date", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize Calendar for April 2026
        viewModel.generateCalendar(4, 2026)

        val empId = com.example.myapplication.MyApplication.sessionManager.fetchEmpIdEms() ?: ""
        if (empId.isNotEmpty()) {
            attendanceViewModel.fetchAttendanceHistory(empId)
        } else {
            Toast.makeText(this, "Employee ID not found. Please login again.", Toast.LENGTH_SHORT).show()
        }

        attendanceViewModel.attendanceHistory.observe(this) { history ->
            viewModel.setAttendanceData(history)
        }

        attendanceViewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAttendanceDialog(record: com.example.myapplication.data.model.AttendanceResponse) {
        // Log the exact data being displayed
        android.util.Log.d("CalendarDialog", "========== SHOWING ATTENDANCE DIALOG ==========")
        android.util.Log.d("CalendarDialog", "Date: ${record.date}")
        android.util.Log.d("CalendarDialog", "In Time: ${record.inTime}")
        android.util.Log.d("CalendarDialog", "Out Time: ${record.outTime}")
        android.util.Log.d("CalendarDialog", "Working Hours: ${record.workingHour}")
        android.util.Log.d("CalendarDialog", "Status: ${record.status}")
        android.util.Log.d("CalendarDialog", "Message: ${record.message}")
        android.util.Log.d("CalendarDialog", "Raw Record: $record")
        android.util.Log.d("CalendarDialog", "====================================================")
        
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_attendance_details)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        // Get dialog views
        val tvDialogDate = dialog.findViewById<TextView>(R.id.tvDialogDate)
        val tvDialogInTime = dialog.findViewById<TextView>(R.id.tvDialogInTime)
        val tvDialogOutTime = dialog.findViewById<TextView>(R.id.tvDialogOutTime)
        val tvDialogWorkingHours = dialog.findViewById<TextView>(R.id.tvDialogWorkingHours)
        val btnDialogClose = dialog.findViewById<Button>(R.id.btnDialogClose)

        // Set data from API response
        tvDialogDate.text = getString(R.string.date_label_format, record.date ?: getString(R.string.not_available))
        tvDialogInTime.text = if (record.inTime.isNullOrEmpty()) getString(R.string.not_checked_in) else record.inTime
        tvDialogOutTime.text = if (record.outTime.isNullOrEmpty()) getString(R.string.not_checked_out) else record.outTime
        tvDialogWorkingHours.text = if (record.workingHour.isNullOrEmpty()) getString(R.string.not_available) else record.workingHour

        // Close button click
        btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}