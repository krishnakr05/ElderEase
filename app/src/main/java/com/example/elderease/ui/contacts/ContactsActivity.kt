package com.example.elderease.ui.contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.home.ContactGridAdapter
import com.example.elderease.ui.settings.SettingsActivity

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerContacts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // 🔥 Load Favourite Contact Numbers
        val raw = getSharedPreferences("elder_favourites", MODE_PRIVATE)
            .getString("fav_contacts", "") ?: ""

        val favNumbers = raw.split(",").filter { it.isNotBlank() }

        val contacts = loadContactsByNumbers(favNumbers)

        recyclerView.adapter = ContactGridAdapter(contacts) { contact ->
            callContact(contact)
        }

        findViewById<TextView>(R.id.txtTitle).text = "Contacts"
        findViewById<TextView>(R.id.txtBattery).visibility = View.GONE

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnContacts).isEnabled = false

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun loadContactsByNumbers(numbers: List<String>): List<ContactInfo> {

        val result = mutableListOf<ContactInfo>()
        val addedPhones = mutableSetOf<String>() // 🔥 prevents duplicates

        val cleanedNumbers = numbers.map {
            it.replace("\\s".toRegex(), "").replace("-", "")
        }

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {

                val id = it.getString(
                    it.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                    )
                )

                val name = it.getString(
                    it.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    )
                )

                var phone = it.getString(
                    it.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )

                // 🔥 Clean formatting
                phone = phone.replace("\\s".toRegex(), "").replace("-", "")

                if (cleanedNumbers.contains(phone) && !addedPhones.contains(phone)) {
                    result.add(ContactInfo(id, name, phone))
                    addedPhones.add(phone)
                }
            }
        }

        return result
    }

    private fun callContact(contact: ContactInfo) {
        startActivity(
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${contact.phone}")
            }
        )
    }
}