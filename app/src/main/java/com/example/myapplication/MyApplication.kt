package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.local.SessionManager

class MyApplication : Application() {

    companion object {
        lateinit var sessionManager: SessionManager
            private set
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
    }
}
