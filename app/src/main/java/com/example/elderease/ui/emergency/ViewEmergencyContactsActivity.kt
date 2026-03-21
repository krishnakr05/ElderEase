package com.example.elderease.ui.emergency

import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.ui.common.ContactRepository

class ViewEmergencyContactsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_emergency_contacts)

        val recycler = findViewById<RecyclerView>(R.id.recyclerContacts)

        recycler.layoutManager = GridLayoutManager(this, 2)

        val phones = ContactRepository.loadSelectedPhones(this)

        val contacts = phones.map {
            Pair(getContactName(it), it)
        }

        recycler.adapter = EmergencyContactAdapter(contacts)
    }

    private fun getContactName(phone: String): String {

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val cursor = contentResolver.query(
            uri,
            null,
            ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
            arrayOf(phone),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {

            val nameIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

            val name = cursor.getString(nameIndex)

            cursor.close()

            return name
        }

        return "Unknown"
    }

}
