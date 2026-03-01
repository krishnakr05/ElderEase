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
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.MainActivity
import com.example.elderease.R
import com.example.elderease.data.storage.CaregiverPrefs
import com.example.elderease.data.storage.SetupState
import com.example.elderease.ui.home.HomeActivity
import com.example.elderease.ui.settings.SettingsActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class CaregiverLoginActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "EXTRA_MODE"
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

        mode = intent.getStringExtra(EXTRA_MODE)
            ?: if (caregiverPrefs.isPinSet()) MODE_VERIFY else MODE_SET

        tvTitle = findViewById(R.id.tvTitle)
        tvError = findViewById(R.id.tvError)
        tvForgotPin = findViewById(R.id.tvForgotPin)
        etPin1 = findViewById(R.id.etPin1)
        etPin2 = findViewById(R.id.etPin2)
        btnAction = findViewById(R.id.btnSetPin)

        updateUIForMode()

        btnAction.setOnClickListener { handlePin() }

        // 🔐 Forgot PIN (long press)
        tvForgotPin.setOnLongClickListener {
            startDeviceAuth()
            true
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

    // ---------------- PIN HANDLING ----------------

    private fun handlePin() {

        val pin1 = etPin1.text.toString().trim()
        val pin2 = if (mode == MODE_SET) etPin2.text.toString().trim() else pin1

        if (pin1.isEmpty() || pin2.isEmpty()) {
            tvError.text = "Please enter PIN"
            return
        }

        val savedPin = caregiverPrefs.getPin()

        when (mode) {

            MODE_SET -> {

                if (pin1 != pin2) {
                    tvError.text = "PINs do not match"
                    return
                }

                caregiverPrefs.savePin(pin1)
                SetupState(this).markPinDone()

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }

            MODE_VERIFY -> {
                if (savedPin == null) {
                    tvError.text = "No caregiver PIN set"
                    return
                }

                if (pin1 == savedPin) {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                } else {
                    attemptsLeft--
                    tvError.text = "Wrong PIN. Attempts left: $attemptsLeft"
                    if (attemptsLeft <= 0) {
                        btnAction.isEnabled = false
                    }
                }
            }
        }
    }

    // ---------------- FORGOT PIN ----------------

    private fun startDeviceAuth() {
        val keyguardManager =
            getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isDeviceSecure) {
            Toast.makeText(
                this,
                "Please enable device lock first",
                Toast.LENGTH_LONG
            ).show()
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
            showResetDialog()
        }
    }

    private fun showResetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Caregiver Access")
            .setMessage(
                "This will reset caregiver PIN."
            )
            .setCancelable(false)
            .setPositiveButton("Reset") { _, _ -> resetApp() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetApp() {
        caregiverPrefs.clearPin()
        caregiverPrefs.resetLock()

        val intent = Intent(this, CaregiverLoginActivity::class.java)
            .putExtra(EXTRA_MODE, MODE_SET)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}