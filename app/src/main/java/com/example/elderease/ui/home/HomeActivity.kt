package com.example.elderease.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.setup.SetupAppsActivity

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SetupAppsActivity.PREFS_NAME, MODE_PRIVATE)
        if (!prefs.getBoolean(SetupAppsActivity.KEY_SETUP_COMPLETE, false)) {
            startActivity(Intent(this, SetupAppsActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        // Apps grid (top)
        val appGrid: RecyclerView = findViewById(R.id.appGrid)
        appGrid.layoutManager = GridLayoutManager(this, 2)

        val packages = prefs.getString(SetupAppsActivity.KEY_SELECTED_PACKAGES, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val apps = loadSelectedApps(packages)
        appGrid.adapter = AppGridAdapter(apps) { app ->
            launchApp(app)
        }

        // Contacts grid (bottom)
        val contactGrid: RecyclerView = findViewById(R.id.contactGrid)
        contactGrid.layoutManager = GridLayoutManager(this, 2)

        val contacts = loadSelectedContacts()
        contactGrid.adapter = ContactGridAdapter(contacts) { contact ->
            callContact(contact)
        }
    }

    /**
     * Loads AppInfo only for the given package names (saved from setup), in the same order.
     * Skips packages that are no longer installed.
     */
    private fun loadSelectedApps(packageNames: List<String>): List<AppInfo> {
        val pm = packageManager
        val result = mutableListOf<AppInfo>()
        for (pkg in packageNames) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: continue
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                result.add(AppInfo(label = label, icon = icon, launchIntent = launchIntent))
            } catch (e: PackageManager.NameNotFoundException) {
                // App uninstalled since setup; skip
            }
        }
        return result
    }

    /**
     * Loads favorite contacts chosen during setup using saved contact IDs.
     * Uses ContactsContract to resolve current name/phone for each ID.
     */
    private fun loadSelectedContacts(): List<ContactInfo> {
        val prefs = getSharedPreferences(SetupAppsActivity.PREFS_NAME, MODE_PRIVATE)
        val raw = prefs.getString("selected_contact_ids", "") ?: ""
        val ids = raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (ids.isEmpty()) return emptyList()

        val result = mutableListOf<ContactInfo>()
        val seen = HashSet<String>()

        // Correct selection with placeholders
        val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
                " IN (" + ids.joinToString(",") { "?" } + ")"

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            selection,
            ids.toTypedArray(),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC"
        )?.use { cursor ->

            // ✅ DEFINE COLUMN INDICES HERE
            val idIdx = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            )
            val nameIdx = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            val phoneIdx = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val seenIds = HashSet<String>()

            while (cursor.moveToNext()) {
                val id = cursor.getString(idIdx)

                // ✅ Prevent duplicates (same contact, multiple numbers)
                if (!seenIds.add(id)) continue

                val name = cursor.getString(nameIdx)
                val phone = cursor.getString(phoneIdx)

                result.add(ContactInfo(id = id, name = name, phone = phone))
            }
        }


        return result
    }


    /**
     * Centralized place to start an app from the grid.
     */
    private fun launchApp(app: AppInfo) {
        app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(app.launchIntent)
    }

    /**
     * Starts the dialer to call the given contact's number.
     */
    private fun callContact(contact: ContactInfo) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phone}")
        }
        startActivity(intent)
    }
}

