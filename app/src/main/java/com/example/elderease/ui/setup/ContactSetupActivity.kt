package com.example.elderease.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.setup.SetupAppsActivity
import android.content.Intent
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import android.widget.Toast
import com.example.elderease.data.storage.SetupState


class ContactSetupActivity : ComponentActivity() {

    private val contacts = mutableListOf<ContactInfo>()
    private lateinit var adapter: ContactAdapter

    private var mode: String = "SETUP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_setup)

        mode = intent.getStringExtra("MODE") ?: "SETUP"

        val recyclerView = findViewById<RecyclerView>(R.id.contactList)
        val saveButton = findViewById<Button>(R.id.saveButton)

        adapter = ContactAdapter(contacts, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        checkPermissionAndLoad()

        saveButton.setOnClickListener {
            saveContacts()
        }
    }

    // -------------------- SAVE LOGIC --------------------

    private fun saveContacts() {

        val selected = contacts.filter { it.isSelected }

        if (selected.isEmpty()) {
            Toast.makeText(
                this,
                "Please select at least one contact",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Save selected contacts
        val selectedIds = selected.map { it.id }

        val prefs =
            getSharedPreferences(
                SetupAppsActivity.PREFS_NAME,
                MODE_PRIVATE
            )

        prefs.edit()
            .putString(
                "selected_contact_ids",
                selectedIds.joinToString(",")
            )
            .apply()

        // EDIT MODE → just exit
        if (mode == "EDIT") {
            Toast.makeText(
                this,
                "Favorite contacts updated",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        // SETUP MODE → mark contacts done
        SetupState(this).markContactsDone()

        // Go to SET PIN
        val intent =
            Intent(this, CaregiverLoginActivity::class.java)
                .putExtra(
                    "MODE",
                    CaregiverLoginActivity.MODE_SET
                )

        startActivity(intent)
        finish()
    }

    // -------------------- PERMISSIONS --------------------

    private fun checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                101
            )
        } else {
            loadContacts()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()
        }
    }

    // -------------------- LOAD CONTACTS --------------------

    private fun loadContacts() {
        contacts.clear()
        val seenContactIds = HashSet<String>()

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(0)
                val name = it.getString(1)
                val phone = it.getString(2)

                if (!seenContactIds.contains(id) && !name.isNullOrBlank()) {
                    seenContactIds.add(id)
                    contacts.add(ContactInfo(id, name, phone))
                }
            }
        }

        contacts.sortBy { it.name.lowercase() }

        if (mode == "EDIT") {
            preloadSelectedContacts()
        }

        adapter.notifyDataSetChanged()
    }

    // -------------------- PRELOAD --------------------

    private fun preloadSelectedContacts() {

        val prefs =
            getSharedPreferences(
                SetupAppsActivity.PREFS_NAME,
                MODE_PRIVATE
            )

        val savedIds =
            prefs.getString("selected_contact_ids", "")
                ?.split(",")
                ?.toSet()
                ?: emptySet()

        contacts.forEach { contact ->
            contact.isSelected = savedIds.contains(contact.id)
        }
    }
}