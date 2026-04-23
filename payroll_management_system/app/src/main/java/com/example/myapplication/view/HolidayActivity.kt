package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.viewmodel.HolidayViewModel
import com.example.myapplication.viewmodel.LoginViewModel

class HolidayActivity : AppCompatActivity() {

    private lateinit var holidayAdapter: HolidayAdapter
    private lateinit var spinnerHolidays: Spinner
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val viewModel: HolidayViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holiday)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        spinnerHolidays = findViewById<Spinner>(R.id.spinnerHolidays)
        val rvHolidays = findViewById<RecyclerView>(R.id.rvHolidays)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        rvHolidays.layoutManager = LinearLayoutManager(this)
        
        viewModel.holidays.observe(this) { holidayList ->
            if (!::holidayAdapter.isInitialized) {
                holidayAdapter = HolidayAdapter(holidayList)
                rvHolidays.adapter = holidayAdapter
            } else {
                holidayAdapter.updateList(holidayList)
            }
            swipeRefreshLayout.isRefreshing = false // Stop animation
        }

        swipeRefreshLayout.setOnRefreshListener {
            loadDataBasedOnSpinner()
        }

        btnBack.setOnClickListener { finish() }

        ivProfile.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("Logout")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Logout") loginViewModel.logout()
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
            }
        }

        val options = arrayOf("My Holidays", "All location Holidays", "My location Holidays")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHolidays.adapter = adapter

        spinnerHolidays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadDataBasedOnSpinner()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Navigation
        findViewById<LinearLayout>(R.id.llHome).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.llPayroll).setOnClickListener {
            startActivity(Intent(this, PayslipActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.llHistory).setOnClickListener {
            startActivity(Intent(this, PayrollHistoryActivity::class.java))
            finish()
        }
    }

    private fun loadDataBasedOnSpinner() {
        when (spinnerHolidays.selectedItemPosition) {
            0 -> viewModel.loadMyHolidays()
            1 -> viewModel.loadAllLocationHolidays()
            2 -> viewModel.loadMyLocationHolidays()
        }
    }
}