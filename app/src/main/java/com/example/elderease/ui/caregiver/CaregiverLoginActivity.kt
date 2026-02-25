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
import com.example.elderease.ui.home.HomeActivity


class CaregiverLoginActivity : AppCompatActivity() {

    private lateinit var etPin1: EditText
    private lateinit var etPin2: EditText
    private lateinit var btnSetPin: Button
    private lateinit var tvError: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvForgotPin: TextView

    private lateinit var caregiverPrefs: CaregiverPrefs

    private val maxAttempts = 3
    private var attemptsLeft = maxAttempts

    private var mode: String = "NORMAL"

    private val requestCodeAuth = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        caregiverPrefs = CaregiverPrefs(this)
        mode = intent.getStringExtra("MODE") ?: "NORMAL"

        setContentView(R.layout.activity_caregiver_login)

        etPin1 = findViewById(R.id.etPin1)
        etPin2 = findViewById(R.id.etPin2)
        btnSetPin = findViewById(R.id.btnSetPin)
        tvError = findViewById(R.id.tvError)
        tvTitle = findViewById(R.id.tvTitle)
        tvForgotPin = findViewById(R.id.tvForgotPin)


        // Verify mode (from settings)
        if (mode == "VERIFY_SETTINGS") {

            tvTitle.text = "Enter Caregiver PIN"
            etPin2.visibility = View.GONE
            btnSetPin.text = "VERIFY PIN"

            tvForgotPin.visibility = View.VISIBLE

        } else {

            tvForgotPin.visibility = View.GONE
        }


        btnSetPin.setOnClickListener {
            handlePin()
        }


        // Long press → forgot PIN
        tvForgotPin.setOnLongClickListener {

            startDeviceAuth()

            true
        }
    }


    // ---------------- Device Authentication ----------------

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

        startActivityForResult(intent, requestCodeAuth)
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCodeAuth) {

            if (resultCode == RESULT_OK) {

                showResetDialog()

            } else {

                Toast.makeText(
                    this,
                    "Authentication failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    // ---------------- Reset Dialog ----------------

    private fun showResetDialog() {

        AlertDialog.Builder(this)
            .setTitle("Reset Caregiver Access")
            .setMessage(
                "This will reset caregiver access.\nElderEase setup will restart."
            )
            .setCancelable(false)

            .setPositiveButton("Reset") { _, _ ->
                resetApp()
            }

            .setNegativeButton("Cancel", null)

            .show()
    }


    // ---------------- Reset App ----------------

    private fun resetApp() {

        caregiverPrefs.clearPin()
        caregiverPrefs.resetLock()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }


    // ---------------- PIN Logic ----------------

    private fun handlePin() {

        val pin1 = etPin1.text.toString().trim()
        val pin2 = etPin2.text.toString().trim()

        val savedPin = caregiverPrefs.getPin()


        // First time setup
        if (savedPin == null) {

            if (pin1.isEmpty() || pin2.isEmpty()) {
                tvError.text = "Please enter PIN in both fields"
                return
            }

            if (pin1 != pin2) {
                tvError.text = "PINs do not match"
                clearFields()
                return
            }

            caregiverPrefs.savePin(pin1)

            Toast.makeText(
                this,
                "PIN set successfully",
                Toast.LENGTH_SHORT
            ).show()

            goNext()

        } else {

            // Verification
            if (pin1.isEmpty()) {
                tvError.text = "Please enter PIN"
                return
            }

            if (pin1 == savedPin) {

                goNext()

            } else {

                attemptsLeft--

                if (attemptsLeft > 0) {

                    tvError.text =
                        "Wrong PIN. Attempts left: $attemptsLeft"

                    clearFields()

                } else {

                    tvError.text =
                        "Too many attempts. Access locked."

                    btnSetPin.isEnabled = false
                }
            }
        }
    }


    private fun clearFields() {

        etPin1.text.clear()
        etPin2.text.clear()
    }


    private fun goNext() {

        if (mode == "VERIFY_SETTINGS") {

            val intent =
                Intent(this, CaregiverSettingsActivity::class.java)

            intent.putExtra("AUTH_OK", true)

            startActivity(intent)

        } else {

            startActivity(
                Intent(this, HomeActivity::class.java)
            )
        }

        finish()
    }
}