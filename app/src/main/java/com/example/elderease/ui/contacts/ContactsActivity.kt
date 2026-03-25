package com.example.elderease.ui.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.home.ContactGridAdapter
import com.example.elderease.ui.settings.SettingsActivity

class ContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val contactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setupContacts()
            } else {
                Toast.makeText(
                    this,
                    "Contacts permission is required to view favourite contacts",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        recyclerView = findViewById(R.id.recyclerContacts)

        val settingsPrefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val grid = settingsPrefs.getInt("home_grid", 2)
        recyclerView.layoutManager = GridLayoutManager(this, grid)

        // UI setup
        findViewById<TextView>(R.id.txtTitle).text = "Contacts"
        findViewById<TextView>(R.id.txtBattery).visibility = View.GONE

        // Back button (YOUR FEATURE ✅)
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnContacts).isEnabled = false

        // 🔥 FIXED settings security
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
            val caregiverEnabled = prefs.getBoolean("caregiver_enabled", false)

            if (caregiverEnabled) {
                val intent = Intent(this, CaregiverLoginActivity::class.java)
                intent.putExtra(
                    CaregiverLoginActivity.EXTRA_MODE,
                    CaregiverLoginActivity.MODE_VERIFY
                )
                startActivity(intent)
            } else {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        // Permission handling (MASTER FEATURE ✅)
        if (hasContactsPermission()) {
            setupContacts()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupContacts() {
        val raw = getSharedPreferences("elder_favourites", MODE_PRIVATE)
            .getString("fav_contacts", "") ?: ""

        val favNumbers = raw.split(",").filter { it.isNotBlank() }

        val contacts = loadContactsByNumbers(favNumbers)

        recyclerView.adapter = ContactGridAdapter(contacts) { contact ->
            callContact(contact)
        }
    }

    private fun loadContactsByNumbers(numbers: List<String>): List<ContactInfo> {
        val result = mutableListOf<ContactInfo>()
        val addedPhones = mutableSetOf<String>()

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
        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val directCall = prefs.getBoolean("direct_call", false)

        val intent = if (directCall) {
            Intent(Intent.ACTION_CALL)
        } else {
            Intent(Intent.ACTION_DIAL)
        }

        intent.data = Uri.parse("tel:${contact.phone}")
        startActivity(intent)
    }
}