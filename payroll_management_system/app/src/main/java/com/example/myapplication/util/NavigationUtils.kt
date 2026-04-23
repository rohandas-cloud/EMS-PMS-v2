package com.example.myapplication.util

import android.app.Activity
import android.content.Intent
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.view.SecondActivity
import com.example.myapplication.view.PayrollHistoryActivity
import com.example.myapplication.view.Calender
import com.example.myapplication.view.PayslipActivity

object NavigationUtils {

    fun setupBottomNavigation(activity: Activity) {
        val llHome = activity.findViewById<View>(R.id.llHome)
        val llPayroll = activity.findViewById<View>(R.id.llPayroll)
        val llCalendar = activity.findViewById<View>(R.id.llCalendar)
        val llHistory = activity.findViewById<View>(R.id.llHistory)

        llHome?.setOnClickListener {
            navigateTo(activity, SecondActivity::class.java)
        }

        llPayroll?.setOnClickListener {
            navigateTo(activity, PayslipActivity::class.java)
        }

        llCalendar?.setOnClickListener {
            navigateTo(activity, Calender::class.java)
        }

        llHistory?.setOnClickListener {
            navigateTo(activity, PayrollHistoryActivity::class.java)
        }
    }

    private fun navigateTo(currentActivity: Activity, targetClass: Class<*>) {
        if (currentActivity.javaClass != targetClass) {
            val intent = Intent(currentActivity, targetClass)
            // If going home, clear stack
            if (targetClass == SecondActivity::class.java) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            currentActivity.startActivity(intent)
            currentActivity.overridePendingTransition(0, 0)
            if (targetClass == SecondActivity::class.java) {
                currentActivity.finish()
            }
        }
    }
}