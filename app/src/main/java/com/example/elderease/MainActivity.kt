package com.example.elderease

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.ui.common.SetupPrefs
import com.example.elderease.ui.home.HomeActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SetupPrefs.isSetupCompleted(this)) {
            // First time → Favourite contacts setup
            startActivity(Intent(this, SetupAppsActivity::class.java))
        } else {
            // Setup already done → Go to Home
            startActivity(Intent(this, HomeActivity::class.java))
        }

        finish()
    }
}