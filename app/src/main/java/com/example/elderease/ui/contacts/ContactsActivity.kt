package com.example.elderease.ui.contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.BaseActivity
import com.example.elderease.R
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.common.ContactRepository
import com.example.elderease.ui.home.ContactGridAdapter
import com.example.elderease.ui.settings.SettingsActivity

class ContactsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

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

        // Settings Button
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            speakAndRun("Opening settings") {
                startActivity(Intent(this, SettingsActivity::class.java))
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