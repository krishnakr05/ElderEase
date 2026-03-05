package com.example.elderease.ui.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.BaseActivity
import com.example.elderease.R
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.common.ContactRepository
import com.example.elderease.ui.home.ContactGridAdapter
import com.example.elderease.ui.caregiver.CaregiverLoginActivity

class ContactsActivity : BaseActivity() {

    private val CONTACT_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        // REQUEST CONTACT PERMISSION
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACT_PERMISSION_CODE
            )
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerContacts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // LOAD DATA
        val contacts = ContactRepository.loadSelectedContacts(this)

        // SET ADAPTER
        recyclerView.adapter = ContactGridAdapter(contacts) { contact ->
            callContact(contact)
        }

        findViewById<TextView>(R.id.txtTitle).text = "Contacts"
        findViewById<TextView>(R.id.txtBattery).visibility = View.GONE

        // Home Button
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            speakAndRun("Going to home") {
                finish()
            }
        }

        // Disable current button
        findViewById<Button>(R.id.btnContacts).isEnabled = false

        // Settings Button (NOW GOES THROUGH PIN)
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            speakAndRun("Opening settings") {
                val intent = Intent(this, CaregiverLoginActivity::class.java)
                intent.putExtra("MODE", CaregiverLoginActivity.MODE_VERIFY)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        speak("Contacts screen")
    }

    private fun callContact(contact: ContactInfo) {
        speakAndRun("Calling ${contact.name}") {
            startActivity(
                Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${contact.phone}")
                }
            )
        }
    }
}