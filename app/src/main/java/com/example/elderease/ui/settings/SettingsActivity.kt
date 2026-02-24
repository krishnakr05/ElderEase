package com.example.elderease.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.ui.setup.ContactSetupActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Button>(R.id.btnEditApps).setOnClickListener {
            startActivity(Intent(this, SetupAppsActivity::class.java).apply {
                putExtra("MODE", "EDIT")
            })
        }

        findViewById<Button>(R.id.btnEditContacts).setOnClickListener {
            startActivity(Intent(this, ContactSetupActivity::class.java).apply {
                putExtra("MODE", "EDIT")
            })
        }

        findViewById<Button>(R.id.btnCustomization).setOnClickListener {
            Toast.makeText(this, "Customization coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnDefaultLauncher).setOnClickListener {
            Toast.makeText(this, "Default launcher setup coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
