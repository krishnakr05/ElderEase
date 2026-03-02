package com.example.elderease.ui.setup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.data.storage.SetupState
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.common.ContactRepository
import com.example.elderease.ui.home.HomeActivity

class ContactSetupActivity : ComponentActivity() {

    private val contacts = mutableListOf<ContactInfo>()
    private val selectedOrder = mutableListOf<ContactInfo>()  // 🔥 maintains preference order
    private lateinit var adapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_setup)

        val mode = intent.getStringExtra("MODE") ?: "SETUP"

        val recyclerView = findViewById<RecyclerView>(R.id.contactList)
        val saveButton = findViewById<Button>(R.id.saveButton)

        adapter = ContactAdapter(contacts) { contact, isChecked ->

            if (isChecked) {

                if (selectedOrder.size >= 3) {
                    Toast.makeText(
                        this,
                        "You can select only 3 emergency contacts",
                        Toast.LENGTH_SHORT
                    ).show()
                    contact.isSelected = false
                    adapter.notifyDataSetChanged()
                    return@ContactAdapter
                }

                selectedOrder.add(contact)

            } else {
                selectedOrder.removeAll { it.phone == contact.phone }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        checkPermissionAndLoad()

        saveButton.setOnClickListener {

            if (selectedOrder.size != 3) {
                Toast.makeText(
                    this,
                    "Please select exactly 3 emergency contacts",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 🔥 Save in selection order
            val phones = selectedOrder.map { it.phone }
            ContactRepository.saveSelectedPhones(this, phones)

            if (mode == "SETUP") {
                SetupState(this).markContactsDone()
            }

            Toast.makeText(
                this,
                "SOS contacts saved successfully",
                Toast.LENGTH_SHORT
            ).show()

            // ✅ Let launcher decide next screen
            if (mode == "SETUP") {
                startActivity(
                    Intent(this, CaregiverLoginActivity::class.java)
                        .putExtra("MODE", CaregiverLoginActivity.MODE_SET)
                )
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
        selectedOrder.clear()

        val uniquePhones = mutableSetOf<String>()

        // ✅ Load previously saved SOS contacts
        val savedPhones = ContactRepository.loadSelectedPhones(this)

        val savedSet = savedPhones.map {
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

                phone = phone.replace("\\s".toRegex(), "")
                    .replace("-", "")

                if (uniquePhones.contains(phone)) continue
                uniquePhones.add(phone)

                val contact = ContactInfo(id, name, phone)

                // ✅ Preselect previously saved contacts
                if (savedSet.contains(phone)) {
                    contact.isSelected = true
                }

                contacts.add(contact)
            }
        }

        contacts.sortBy { it.name.lowercase() }

        // ✅ Restore exact selection order
        savedPhones.forEach { savedPhone ->
            contacts.find { it.phone == savedPhone }?.let {
                selectedOrder.add(it)
            }
        }

        adapter.notifyDataSetChanged()
    }
}