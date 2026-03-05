package com.example.elderease.ui.emergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.elderease.R

class EmergencyActivity : AppCompatActivity() {


    private lateinit var manager: EmergencyManager
    private lateinit var btnHelp: Button
    private lateinit var btnCancel: Button
    private lateinit var btnViewContacts: Button

    private var timer: CountDownTimer? = null
    private var countdownStarted = false

    private val PERMISSION_REQUEST_EMERGENCY = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        manager = EmergencyManager(this)

        btnHelp = findViewById(R.id.btnHelp)
        btnCancel = findViewById(R.id.btnCancel)
        btnViewContacts = findViewById(R.id.btnViewContacts)

        btnCancel.visibility = View.INVISIBLE

        btnHelp.setOnClickListener {
            if (!countdownStarted) {
                startCountdown()
                countdownStarted = true
                btnCancel.visibility = View.VISIBLE
            }
        }

        btnCancel.setOnClickListener {
            timer?.cancel()
            Toast.makeText(this, "Emergency cancelled", Toast.LENGTH_SHORT).show()
            finish()
        }

        // ⭐ Open Emergency Contacts Screen
        btnViewContacts.setOnClickListener {
            val intent = Intent(this, ViewEmergencyContactsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startCountdown() {

        timer = object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                btnCancel.text = "CANCEL ($seconds)"
            }

            override fun onFinish() {
                btnCancel.text = "Sending help..."
                checkPermissionsAndTrigger()
            }

        }.start()
    }

    private fun checkPermissionsAndTrigger() {

        val callGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val smsGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val locationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (callGranted && smsGranted && locationGranted) {
            triggerSOS()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_REQUEST_EMERGENCY
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_EMERGENCY &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            triggerSOS()
        } else {
            Toast.makeText(this, "Permissions required for SOS", Toast.LENGTH_LONG).show()
        }
    }

    private fun triggerSOS() {
        manager.triggerSOS()
        finish()
    }


}
