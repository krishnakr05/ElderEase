package com.example.elderease.ui.setup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.home.HomeActivity

class CaregiverSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caregiver_setup)

        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)

        findViewById<Button>(R.id.btnSetup).setOnClickListener {
            startActivity(
                Intent(this, CaregiverLoginActivity::class.java).apply {
                    putExtra(
                        CaregiverLoginActivity.EXTRA_MODE,
                        CaregiverLoginActivity.MODE_SET
                    )
                }
            )
            finish()
        }

        findViewById<Button>(R.id.btnSkip).setOnClickListener {
            prefs.edit().putBoolean("caregiver_enabled", false).apply()

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}