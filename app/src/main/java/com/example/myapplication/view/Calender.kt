package com.example.myapplication.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.AttendanceViewModel
import com.example.myapplication.viewmodel.CalendarViewModel
import com.example.myapplication.data.model.*
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

class Calender : AppCompatActivity() {
    
    private val viewModel: CalendarViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    
    // UI Components
    private lateinit var tvRealTimeClock: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvPresentCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvWeekendCount: TextView
    private lateinit var tvAverageTime: TextView
    private lateinit var vpCalendar: ViewPager2
    private lateinit var tvInTimeCard: TextView
    private lateinit var tvOutTimeCard: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var pbCalendar: ProgressBar
    
    // States
    private val clockHandler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable
    private var navigationCalendar = Calendar.getInstance()
    private lateinit var monthAdapter: CalendarMonthAdapter
    private val startPosition = 1200 // Middle of 2400 (200 years)
    private var isProgrammaticScroll = false
    private var empId: String = ""
    private var initialMonth: Int = 0
    private var initialYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        // 1. Bind Views
        try {
            bindViews()
        } catch (e: Exception) {
            Log.e("CalenderActivity", "CRITICAL ERROR: Failed to bind views", e)
            Toast.makeText(this, "UI Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Setup States
        empId = com.example.myapplication.MyApplication.sessionManager.fetchEmpIdEms().orEmpty()
        Log.d("CalenderActivity", "Fetching attendance for EMS empId: $empId")

        // 3. Setup Components
        setupCalendar()
        setupRealTimeClock()
        
        // 4. Set Initial Content
        tvTitle.text = "Attendance"
        tvSelectedDate.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(navigationCalendar.time)
        
        // 5. Setup Observers
        setupObservers()

        // 6. Navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        NavigationUtils.setupBottomNavigation(this)

        // 7. Initial Data Fetch
        if (empId.isNotEmpty()) {
            // Trigger initial dummy data while waiting for API
            viewModel.setAttendanceData(emptyList())
            
            attendanceViewModel.fetchAttendanceHistory(empId)
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            attendanceViewModel.fetchDailyAttendance(empId, todayDate)
        }
    }

    private fun bindViews() {
        tvRealTimeClock = findViewById(R.id.tvRealTimeClock)
        tvTitle = findViewById(R.id.tvTitle)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvPresentCount = findViewById(R.id.tvPresentCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        tvWeekendCount = findViewById(R.id.tvWeekendCount)
        tvAverageTime = findViewById(R.id.tvAverageTime)
        vpCalendar = findViewById(R.id.vpCalendar)
        tvInTimeCard = findViewById(R.id.tvInTime)
        tvOutTimeCard = findViewById(R.id.tvOutTime)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        pbCalendar = findViewById(R.id.pbCalendar)
    }

    private fun setupCalendar() {
        monthAdapter = CalendarMonthAdapter { day ->
            viewModel.onDateSelected(day)
        }
        vpCalendar.adapter = monthAdapter
        
        isProgrammaticScroll = true
        vpCalendar.setCurrentItem(startPosition, false)
        isProgrammaticScroll = false

        vpCalendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (isProgrammaticScroll) return
                
                val diff = position - startPosition
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, diff)
                
                Log.d("CalenderActivity", "Swiped to: ${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}")
                
                if (empId.isNotEmpty()) {
                    viewModel.currentMonth = cal.get(Calendar.MONTH) + 1
                    viewModel.currentYear = cal.get(Calendar.YEAR)
                    viewModel.generateCalendar(viewModel.currentMonth, viewModel.currentYear)
                    attendanceViewModel.fetchAttendanceHistory(empId)
                }
            }
        })

        findViewById<ImageView>(R.id.btnPrevDay).setOnClickListener {
            val currentDateStr = viewModel.selectedDateString.value ?: return@setOnClickListener
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val cal = Calendar.getInstance()
                cal.time = dateFormat.parse(currentDateStr) ?: return@setOnClickListener
                cal.add(Calendar.DAY_OF_MONTH, -1)
                
                val newDateStr = dateFormat.format(cal.time)
                viewModel.setSelectedDate(newDateStr)
                
                // Keep the ViewPager position synchronized with the ViewModel's month
                val diff = ((viewModel.currentYear - initialYear) * 12) + (viewModel.currentMonth - initialMonth)
                val targetPosition = startPosition + diff
                if (vpCalendar.currentItem != targetPosition) {
                    vpCalendar.setCurrentItem(targetPosition, true)
                }
            } catch (e: Exception) {
                Log.e("CalenderActivity", "Error navigating to previous day", e)
            }
        }
        findViewById<ImageView>(R.id.btnNextDay).setOnClickListener {
            val currentDateStr = viewModel.selectedDateString.value ?: return@setOnClickListener
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val cal = Calendar.getInstance()
                cal.time = dateFormat.parse(currentDateStr) ?: return@setOnClickListener
                cal.add(Calendar.DAY_OF_MONTH, 1)
                
                val newDateStr = dateFormat.format(cal.time)
                viewModel.setSelectedDate(newDateStr)
                
                // Keep the ViewPager position synchronized with the ViewModel's month
                val diff = ((viewModel.currentYear - initialYear) * 12) + (viewModel.currentMonth - initialMonth)
                val targetPosition = startPosition + diff
                if (vpCalendar.currentItem != targetPosition) {
                    vpCalendar.setCurrentItem(targetPosition, true)
                }
            } catch (e: Exception) {
                Log.e("CalenderActivity", "Error navigating to next day", e)
            }
        }
        
        initialMonth = navigationCalendar.get(Calendar.MONTH) + 1
        initialYear = navigationCalendar.get(Calendar.YEAR)
        viewModel.generateCalendar(initialMonth, initialYear)
        
        // Ensure month name is set immediately
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = monthYearFormat.format(navigationCalendar.time)
    }

    private fun setupObservers() {
        viewModel.currentMonthName.observe(this) { tvMonthYear.text = it }
        viewModel.presentCount.observe(this) { tvPresentCount.text = getString(R.string.days_count_format, it) }
        viewModel.absentCount.observe(this) { tvAbsentCount.text = getString(R.string.days_count_format, it) }
        viewModel.weekendCount.observe(this) { tvWeekendCount.text = getString(R.string.days_count_format, it) }
        viewModel.averageWorkingHours.observe(this) { tvAverageTime.text = it }

        viewModel.calendarDays.observe(this) { daysList ->
            val currentPos = vpCalendar.currentItem
            monthAdapter.updateData(mapOf(currentPos to daysList))
        }

        viewModel.selectedDateString.observe(this) { dateStr ->
            Log.d("CalenderActivity", "Date selected: $dateStr")
            attendanceViewModel.fetchDailyAttendance(empId, dateStr)
        }

        attendanceViewModel.dailyAttendance.observe(this) { record ->
            // Try to use the record from history if API call fails
            var displayRecord = record
            if (displayRecord == null) {
                val currentDateStr = viewModel.selectedDateString.value
                displayRecord = viewModel.selectedAttendance.value
                if (displayRecord?.date != currentDateStr && currentDateStr != null) {
                   displayRecord = null
                }
            }

            if (displayRecord != null) {
                // Sync fetched record with summary
                viewModel.updateTodayRecord(displayRecord)
                
                // Update the Details Card
                tvSelectedDate.text = displayRecord.date?.let { dateStr ->
                    try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
                        date?.let { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(it) } ?: dateStr
                    } catch (e: Exception) { dateStr }
                } ?: "No Date"
                
                tvInTimeCard.text = displayRecord.inTime ?: "-- : --"
                tvOutTimeCard.text = displayRecord.outTime ?: "-- : --"
                
                // Show dialog ONLY if this was explicitly clicked
                if (displayRecord.date == viewModel.selectedDateString.value && record != null) { // Only show dialog if it came from the API call
                    showAttendanceDialog(displayRecord)
                }
            } else {
                tvInTimeCard.text = "-- : --"
                tvOutTimeCard.text = "-- : --"
                // Keep the selected date visible even if no record exists
                tvSelectedDate.text = viewModel.selectedDateString.value?.let { dateStr ->
                    try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
                        date?.let { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(it) } ?: dateStr
                    } catch (e: Exception) { dateStr }
                } ?: SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
            }
        }

        attendanceViewModel.attendanceHistory.observe(this) { history ->
            if (history != null) {
                Log.d("CalenderActivity", "Attendance history received: ${history.size} records")
                viewModel.setAttendanceData(history)
            } else {
                viewModel.setAttendanceData(emptyList())
            }
        }

        attendanceViewModel.isLoading.observe(this) { isLoading ->
            pbCalendar.visibility = if (isLoading) View.VISIBLE else View.GONE
            vpCalendar.alpha = if (isLoading) 0.5f else 1.0f
        }

        attendanceViewModel.errorMessage.observe(this) { _ ->
            // Toast removed to prevent fetching failures from showing
        }
    }

    private fun setupRealTimeClock() {
        clockRunnable = object : Runnable {
            override fun run() {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                tvRealTimeClock.text = time
                clockHandler.postDelayed(this, 1000)
            }
        }
        clockHandler.post(clockRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::clockRunnable.isInitialized) {
            clockHandler.removeCallbacks(clockRunnable)
        }
    }

    private fun showAttendanceDialog(record: AttendanceResponse) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_attendance_details)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val tvDialogDate = dialog.findViewById<TextView>(R.id.tvDialogDate)
        val tvDialogInTime = dialog.findViewById<TextView>(R.id.tvDialogInTime)
        val tvDialogOutTime = dialog.findViewById<TextView>(R.id.tvDialogOutTime)
        val tvDialogWorkingHours = dialog.findViewById<TextView>(R.id.tvDialogWorkingHours)
        val btnDialogClose = dialog.findViewById<Button>(R.id.btnDialogClose)

        tvDialogDate.text = getString(R.string.date_label_format, record.date ?: getString(R.string.not_available))
        tvDialogInTime.text = record.inTime ?: "--:--"
        tvDialogOutTime.text = record.outTime ?: "--:--"
        tvDialogWorkingHours.text = record.workingHour ?: "--:--"

        btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}