package com.example.elderease

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.data.storage.CaregiverPrefs
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.home.HomeActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val caregiverPrefs = CaregiverPrefs(this)

        val isPinSet = caregiverPrefs.isPinSet()

        if (!isPinSet) {

            // First time setup
            val intent =
                Intent(this, CaregiverLoginActivity::class.java)

            intent.putExtra("MODE", "NORMAL")

            startActivity(intent)

        } else {

            // PIN already exists → go to Home
            startActivity(
                Intent(this, HomeActivity::class.java)
            )
        }

        finish()
    }
}