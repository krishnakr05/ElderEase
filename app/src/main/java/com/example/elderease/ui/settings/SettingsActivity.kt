package com.example.elderease.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.ui.setup.ContactSetupActivity
import com.example.elderease.ui.setup.FavouriteContactSetupActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class SettingsActivity : AppCompatActivity() {

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

        findViewById<android.view.View>(R.id.rowCustomization).setOnClickListener {
            Toast.makeText(this, "Customization coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.rowDefaultLauncher).setOnClickListener {
            Toast.makeText(this, "Default launcher setup coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
