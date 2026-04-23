package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MyApplication
import com.example.myapplication.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivLogo = findViewById<ImageView>(R.id.ivSplashLogo)
        
        // Load and start animation
        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        ivLogo.startAnimation(animation)

        // Delay for 2 seconds then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndNavigate()
        }, 2500)
    }

    private fun checkSessionAndNavigate() {
        val sessionManager = MyApplication.sessionManager
        
        if (sessionManager.isLoggedIn()) {
            // If already logged in, go to Dashboard (SecondActivity)
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        } else {
            // If not logged in, go to Login screen (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        
        finish() // Close SplashActivity so user can't go back to it
    }
}
