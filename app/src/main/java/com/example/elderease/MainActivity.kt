package com.example.elderease

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open HomeActivity directly
        startActivity(Intent(this, HomeActivity::class.java))

        // Close MainActivity
        finish()
    }
}
