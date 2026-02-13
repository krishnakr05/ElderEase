package com.example.elderease.ui.contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.common.ContactRepository
import com.example.elderease.ui.home.ContactGridAdapter

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerContacts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        //LOAD DATA 
        val contacts = ContactRepository.loadSelectedContacts(this)

        // SET ADAPTER
        recyclerView.adapter = ContactGridAdapter(contacts) { contact ->
            callContact(contact)
        }
        findViewById<TextView>(R.id.txtTitle).text = "Contacts"
    }

    private fun callContact(contact: ContactInfo) {
        startActivity(
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${contact.phone}")
            }
        )
    }
}

