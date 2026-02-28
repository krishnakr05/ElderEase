package com.example.elderease.ui.caregiver

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.elderease.R
import com.example.elderease.data.storage.CaregiverPrefs
import com.example.elderease.data.storage.SetupState
import com.example.elderease.ui.home.HomeActivity
import com.example.elderease.ui.settings.SettingsActivity
import com.example.elderease.BaseActivity

class CaregiverLoginActivity : BaseActivity() {

    companion object {
        const val MODE_SET = "SET_PIN"
        const val MODE_VERIFY = "VERIFY_PIN"
        private const val REQUEST_DEVICE_AUTH = 1001
    }

    private lateinit var caregiverPrefs: CaregiverPrefs
    private lateinit var mode: String

    private lateinit var tvTitle: TextView
    private lateinit var tvError: TextView
    private lateinit var tvForgotPin: TextView
    private lateinit var etPin1: EditText
    private lateinit var etPin2: EditText
    private lateinit var btnAction: Button

    private val MAX_ATTEMPTS = 3
    private var attemptsLeft = MAX_ATTEMPTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caregiver_login)

        caregiverPrefs = CaregiverPrefs(this)

        mode = intent.getStringExtra("MODE")
            ?: if (caregiverPrefs.isPinSet()) MODE_VERIFY else MODE_SET

        tvTitle = findViewById(R.id.tvTitle)
        tvError = findViewById(R.id.tvError)
        tvForgotPin = findViewById(R.id.tvForgotPin)
        etPin1 = findViewById(R.id.etPin1)
        etPin2 = findViewById(R.id.etPin2)
        btnAction = findViewById(R.id.btnSetPin)

        updateUIForMode()

        btnAction.setOnClickListener {
            vibrate()
            speak("Processing")
            handlePin()
        }

        tvForgotPin.setOnLongClickListener {
            vibrate()
            speak("Device authentication required")
            startDeviceAuth()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if (mode == MODE_SET) {
            speak("Set caregiver PIN")
        } else {
            speak("Verify caregiver PIN")
        }
    }

    private fun updateUIForMode() {
        when (mode) {

            MODE_SET -> {
                tvTitle.text = "Set Caregiver PIN"
                etPin1.hint = "Enter new PIN"
                etPin2.hint = "Confirm new PIN"
                etPin2.visibility = View.VISIBLE
                btnAction.text = "Set PIN"
                tvForgotPin.visibility = View.GONE
            }

            MODE_VERIFY -> {
                tvTitle.text = "Verify Caregiver PIN"
                etPin1.hint = "Enter PIN"
                etPin2.visibility = View.GONE
                btnAction.text = "Verify"
                tvForgotPin.visibility = View.VISIBLE
            }
        }
    }

    private fun handlePin() {

        val pin1 = etPin1.text.toString().trim()
        val pin2 = if (mode == MODE_SET) etPin2.text.toString().trim() else pin1

        if (pin1.isEmpty() || pin2.isEmpty()) {
            tvError.text = "Please enter PIN"
            speak("Please enter PIN")
            return
        }

        val savedPin = caregiverPrefs.getPin()

        when (mode) {

            MODE_SET -> {

                if (pin1 != pin2) {
                    tvError.text = "PINs do not match"
                    speak("PINs do not match")
                    return
                }

                caregiverPrefs.savePin(pin1)
                SetupState(this).markPinDone()

                speakAndRun("PIN set successfully") {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }

            MODE_VERIFY -> {

                if (savedPin == null) {
                    tvError.text = "No caregiver PIN set"
                    speak("No caregiver PIN set")
                    return
                }

                if (pin1 == savedPin) {

                    speakAndRun("Access granted") {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        finish()
                    }

                } else {

                    attemptsLeft--
                    tvError.text = "Wrong PIN. Attempts left: $attemptsLeft"
                    speak("Wrong PIN")

                    if (attemptsLeft <= 0) {
                        btnAction.isEnabled = false
                        speak("Too many attempts. Access locked")
                    }
                }
            }
        }
    }

    private fun startDeviceAuth() {
        val keyguardManager =
            getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isDeviceSecure) {
            Toast.makeText(
                this,
                "Please enable device lock first",
                Toast.LENGTH_LONG
            ).show()
            speak("Please enable device lock first")
            return
        }

        val intent =
            keyguardManager.createConfirmDeviceCredentialIntent(
                "Verify Caregiver",
                "Confirm device lock to reset ElderEase"
            )

        startActivityForResult(intent, REQUEST_DEVICE_AUTH)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_DEVICE_AUTH && resultCode == RESULT_OK) {
            speak("Authentication successful")
            showResetDialog()
        }
    }

    private fun showResetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Caregiver Access")
            .setMessage("This will reset caregiver PIN.")
            .setCancelable(false)
            .setPositiveButton("Reset") { _, _ ->
                speak("Resetting caregiver access")
                resetApp()
            }
            .setNegativeButton("Cancel") { _, _ ->
                speak("Cancelled")
            }
            .show()
    }

    private fun resetApp() {
        caregiverPrefs.clearPin()
        caregiverPrefs.resetLock()

        val intent =
            Intent(this, CaregiverLoginActivity::class.java)
                .putExtra("MODE", MODE_SET)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}