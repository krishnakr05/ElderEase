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
import com.example.elderease.BaseActivity
import com.example.elderease.R

class EmergencyActivity : BaseActivity() {

    private lateinit var btnCancel: Button
    private lateinit var btnHelp: Button
    private lateinit var btnBack: ImageView

    private var timer: CountDownTimer? = null
    private var secondsLeft = 5
    private var countdownStarted = false

    private val PERMISSION_REQUEST_EMERGENCY = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        btnCancel = findViewById(R.id.btnCancel)
        btnHelp = findViewById(R.id.btnHelp)
        btnBack = findViewById(R.id.btnBack)

        btnCancel.visibility = View.INVISIBLE

        // Back button
        btnBack.setOnClickListener {
            speakAndRun("Returning to home") {
                finish()
            }
        }

        // HELP button
        btnHelp.setOnClickListener {
            if (!countdownStarted) {
                vibrate()
                speak("Emergency countdown started")
                startCountdown()
                countdownStarted = true
                btnCancel.visibility = View.VISIBLE
            }
        }

        // Cancel button
        btnCancel.setOnClickListener {
            timer?.cancel()
            speakAndRun("Emergency cancelled") {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        speak("Emergency screen")
    }

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
        speak("Requesting permissions")
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

                // Speak only at 5 and 1
                if (secondsLeft == 5 || secondsLeft == 1) {
                    speak(secondsLeft.toString())
                }

                secondsLeft--
            }

            override fun onFinish() {
                btnCancel.text = "Calling..."
                speak("Emergency activated")

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
                speak("Permissions granted")
                EmergencyManager(this).triggerSOS()
                finish()
            } else {
                speak("Permission denied")
            }
        }
    }
}