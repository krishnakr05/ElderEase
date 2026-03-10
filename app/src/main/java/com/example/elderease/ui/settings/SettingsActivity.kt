package com.example.elderease.ui.settings

import android.content.Intent
import android.os.Bundle
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

        findViewById<android.view.View>(R.id.rowEditEmergencyContacts)
            .setOnClickListener {
                startActivity(
                    Intent(this, ContactSetupActivity::class.java).apply {
                        putExtra("MODE", "EDIT_SOS")
                    }
                )
            }

        val customization = findViewById<android.view.View>(R.id.rowCustomization)

        customization.setOnClickListener {
            customizationLauncher.launch(
                Intent(this, CustomizationActivity::class.java)
            )
        }

        findViewById<android.view.View>(R.id.rowDefaultLauncher).setOnClickListener {

            try {
                val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {

                // fallback
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                startActivity(intent)
            }
        }
    }
}
