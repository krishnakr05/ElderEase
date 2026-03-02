package com.example.elderease.ui.setup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.data.storage.SetupState
import com.example.elderease.model.ContactInfo

class FavouriteContactSetupActivity : AppCompatActivity() {

    private val contacts = mutableListOf<ContactInfo>()
    private val selectedContacts = mutableListOf<String>()
    private lateinit var adapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_contact_setup)

        val mode = intent.getStringExtra("MODE") ?: "SETUP"

        val recyclerView = findViewById<RecyclerView>(R.id.contactList)
        val saveButton = findViewById<Button>(R.id.saveButton)

        adapter = ContactAdapter(contacts) { contact, isChecked ->
            if (isChecked) {
                if (!selectedContacts.contains(contact.phone)) {
                    selectedContacts.add(contact.phone)
                }
            } else {
                selectedContacts.remove(contact.phone)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        checkPermissionAndLoad()

        saveButton.setOnClickListener {

            if (selectedContacts.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please select at least one favourite contact",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // ✅ Save favourites
            getSharedPreferences("elder_favourites", MODE_PRIVATE)
                .edit()
                .putString("fav_contacts", selectedContacts.joinToString(","))
                .apply()

            SetupState(this).markFavouriteContactsDone()

            if (mode == "SETUP") {
                startActivity(Intent(this, ContactSetupActivity::class.java))
            }

            finish()
        }
    }

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
        } else {
            Toast.makeText(
                this,
                "Permission required to read contacts",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadContacts() {

        contacts.clear()
        selectedContacts.clear()

        // 🔥 Load saved favourites
        val savedPhones = getSharedPreferences("elder_favourites", MODE_PRIVATE)
            .getString("fav_contacts", "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val savedSet = savedPhones.map {
            it.replace("\\s".toRegex(), "").replace("-", "")
        }

        val uniquePhones = mutableSetOf<String>()

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

                phone = phone.replace("\\s".toRegex(), "")
                    .replace("-", "")

                if (uniquePhones.contains(phone)) continue
                uniquePhones.add(phone)

                val contact = ContactInfo(id, name, phone)

                // ✅ PRESELECT saved favourites
                if (savedSet.contains(phone)) {
                    contact.isSelected = true
                    selectedContacts.add(phone)
                }

                contacts.add(contact)
            }
        }

        contacts.sortBy { it.name.lowercase() }
        adapter.notifyDataSetChanged()
    }
}