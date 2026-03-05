package com.example.elderease.ui.common

import android.content.Context
import android.provider.ContactsContract
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.setup.SetupAppsActivity

object ContactRepository {

    fun loadSelectedContacts(context: Context): List<ContactInfo> {

        val prefs = context.getSharedPreferences(
            SetupAppsActivity.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val raw = prefs.getString("selected_contact_ids", "") ?: ""

        val ids = raw.split(",").filter { it.isNotBlank() }

        if (ids.isEmpty()) return emptyList()

        val result = mutableListOf<ContactInfo>()

        val selection =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
                    " IN (" + ids.joinToString(",") { "?" } + ")"

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            selection,
            ids.toTypedArray(),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +
                    " COLLATE NOCASE ASC"
        )?.use { cursor ->

            val idIdx = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            )

            val nameIdx = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )

            val phoneIdx = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val seen = HashSet<String>()

            while (cursor.moveToNext()) {

                val id = cursor.getString(idIdx)

                if (seen.contains(id)) continue
                seen.add(id)

                result.add(
                    ContactInfo(
                        id = id,
                        name = cursor.getString(nameIdx),
                        phone = cursor.getString(phoneIdx)
                    )
                )
            }
        }

        return result
    }

    fun saveSelectedContacts(context: Context, contacts: List<ContactInfo>) {

        val prefs = context.getSharedPreferences(
            SetupAppsActivity.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val ids = contacts
            .filter { it.isSelected }
            .joinToString(",") { it.id }

        prefs.edit()
            .putString("selected_contact_ids", ids)
            .apply()
    }
}