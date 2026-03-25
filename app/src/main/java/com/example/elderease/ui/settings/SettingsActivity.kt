package com.example.elderease.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.elderease.R
import com.example.elderease.ui.setup.ContactSetupActivity
import com.example.elderease.ui.setup.FavouriteContactSetupActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class SettingsActivity : AppCompatActivity() {

    private val customizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(RESULT_OK)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 🔐 SECURITY CHECK (ADDED)
        val isVerified = getSharedPreferences("ElderEasePrefs", Context.MODE_PRIVATE)
            .getBoolean("isVerified", false)

        if (!isVerified) {
            startActivity(
                Intent(
                    this,
                    com.example.elderease.ui.caregiver.CaregiverLoginActivity::class.java
                )
            )
            finish()
            return
        }

        findViewById<android.widget.Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<android.view.View>(R.id.rowEditApps).setOnClickListener {
            startActivity(Intent(this, SetupAppsActivity::class.java).apply {
                putExtra("MODE", "EDIT")
            })
        }

        findViewById<android.view.View>(R.id.rowEditContacts).setOnClickListener {
            startActivity(Intent(this, FavouriteContactSetupActivity::class.java).apply {
                putExtra("MODE", "EDIT")
            })
        }

        findViewById<android.view.View>(R.id.rowEditEmergencyContacts).setOnClickListener {
            startActivity(
                Intent(this, ContactSetupActivity::class.java).apply {
                    putExtra("MODE", "EDIT_SOS")
                }
            )
        }

        findViewById<android.view.View>(R.id.rowCustomization).setOnClickListener {
            customizationLauncher.launch(
                Intent(this, CustomizationActivity::class.java)
            )
        }

        findViewById<android.view.View>(R.id.rowDefaultLauncher).setOnClickListener {

            try {
                startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
            } catch (e: Exception) {
                startActivity(Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            }
        }
    }

    // 🔐 RESET VERIFICATION WHEN LEAVING SETTINGS (ADDED)
    override fun onDestroy() {
        super.onDestroy()

        getSharedPreferences("ElderEasePrefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("isVerified", false)
            .apply()
    }
}
