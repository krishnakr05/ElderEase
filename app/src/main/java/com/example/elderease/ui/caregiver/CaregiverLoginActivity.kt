package com.example.elderease.ui.caregiver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.R
import com.example.elderease.data.storage.CaregiverPrefs

class CaregiverLoginActivity : AppCompatActivity() {

    private lateinit var prefs: CaregiverPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caregiver_login)

        prefs = CaregiverPrefs(this)

        val etPin = findViewById<EditText>(R.id.etPin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {

            val enteredPin = etPin.text.toString().trim()
            val savedPin = prefs.getPin()

            if (enteredPin == savedPin) {

                Toast.makeText(
                    this,
                    "Login Successful",
                    Toast.LENGTH_SHORT
                ).show()

                // Open Settings Screen
                val intent = Intent(
                    this,
                    CaregiverSettingsActivity::class.java
                )
                startActivity(intent)

                etPin.text.clear()

            } else {

                Toast.makeText(
                    this,
                    "Incorrect PIN",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
