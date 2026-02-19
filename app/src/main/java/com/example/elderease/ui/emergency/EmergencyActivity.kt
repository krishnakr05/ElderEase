package com.example.elderease.ui.emergency

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.ui.emergency.EmergencyManager


class EmergencyActivity : AppCompatActivity() {

    private lateinit var btnCancel: Button
    private lateinit var btnHelp: Button
    private lateinit var btnBack: ImageView

    private var timer: CountDownTimer? = null
    private var secondsLeft = 5
    private var countdownStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        btnCancel = findViewById(R.id.btnCancel)
        btnHelp = findViewById(R.id.btnHelp)
        btnBack = findViewById(R.id.btnBack)

        // Hide cancel button initially
        btnCancel.visibility = View.INVISIBLE

        // Back button → return to Home
        btnBack.setOnClickListener {
            finish()
        }

        // Start countdown ONLY when HELP is pressed
        btnHelp.setOnClickListener {
            if (!countdownStarted) {
                startCountdown()
                countdownStarted = true
                btnCancel.visibility = View.VISIBLE
            }
        }

        // Cancel button stops countdown
        btnCancel.setOnClickListener {
            timer?.cancel()
            finish()
        }
    }

    private val PERMISSION_REQUEST_EMERGENCY = 200

    private fun hasEmergencyPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestEmergencyPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS
            ),
            PERMISSION_REQUEST_EMERGENCY
        )
    }


    private fun startCountdown() {
        secondsLeft = 5

        timer = object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                btnCancel.text = "CANCEL (${secondsLeft}s)"
                secondsLeft--
            }

            override fun onFinish() {
                btnCancel.text = "Calling..."

                if (hasEmergencyPermissions()) {
                    EmergencyManager(this@EmergencyActivity).triggerSOS()
                    finish()
                } else {
                    requestEmergencyPermissions()
                }
            }

        }.start()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_EMERGENCY) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                EmergencyManager(this).triggerSOS()
                finish()
            }
        }
    }

}
