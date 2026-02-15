package com.example.elderease.ui.emergency

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R

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

    private fun startCountdown() {
        secondsLeft = 5

        timer = object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                btnCancel.text = "CANCEL (${secondsLeft}s)"
                secondsLeft--
            }

            override fun onFinish() {
                btnCancel.text = "Calling..."
                // TODO: Add emergency call / SMS / GPS logic
            }

        }.start()
    }
}
