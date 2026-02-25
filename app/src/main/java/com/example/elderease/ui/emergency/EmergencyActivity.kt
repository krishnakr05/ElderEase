package com.example.elderease.ui.emergency

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.elderease.R

class EmergencyActivity : AppCompatActivity() {

    private lateinit var manager: EmergencyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        manager = EmergencyManager(this)

        // 🔥 IMPORTANT: Your XML button id is btnHelp
        findViewById<Button>(R.id.btnHelp).setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {

        val callGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val smsGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!callGranted || !smsGranted) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS
                ),
                200
            )

        } else {
            manager.triggerSOS()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 200 &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            manager.triggerSOS()
        } else {
            Toast.makeText(
                this,
                "Permissions required for SOS",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}