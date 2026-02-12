package com.example.elderease.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.provider.ContactsContract
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.ui.emergency.EmergencyActivity
import com.example.elderease.ui.settings.SettingsActivity
import com.example.elderease.ui.voice.VoiceHelpActivity
import java.text.SimpleDateFormat
import java.util.*
import com.example.elderease.model.AppInfo
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.setup.SetupAppsActivity
import android.util.Log

class HomeActivity : AppCompatActivity() {

    private lateinit var txtTime: android.widget.TextView
    private lateinit var txtDate: android.widget.TextView
    private lateinit var txtBattery: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SetupAppsActivity.PREFS_NAME, MODE_PRIVATE)
        if (!prefs.getBoolean(SetupAppsActivity.KEY_SETUP_COMPLETE, false)) {
            startActivity(Intent(this, SetupAppsActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerApps)

        recyclerView.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, 2)

        recyclerView.isNestedScrollingEnabled = false


        val packages = prefs.getString(SetupAppsActivity.KEY_SELECTED_PACKAGES, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val apps = loadSelectedApps(packages)

        Log.d("HomeActivity", "Selected packages: $packages")
        Log.d("HomeActivity", "Loaded apps count: ${apps.size}")


        recyclerView.adapter = AppAdapter(apps) { app ->
            launchApp(app)
        }

        txtTime = findViewById(R.id.txtTime)
        txtDate = findViewById(R.id.txtDate)
        txtBattery = findViewById(R.id.txtBattery)

        startClock()
        monitorBattery()

        findViewById<android.widget.Button>(R.id.btnHelp).setOnClickListener {
            startActivity(Intent(this, VoiceHelpActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.btnManual).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }/*
        val appGrid: RecyclerView = findViewById(R.id.appGrid)
        appGrid.layoutManager = GridLayoutManager(this, 2)

        val packages = prefs.getString(SetupAppsActivity.KEY_SELECTED_PACKAGES, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        val selectedApps = loadSelectedApps(packages)
        appGrid.adapter = AppGridAdapter(apps) { app ->
            launchApp(app)
        }

        val contactGrid: RecyclerView = findViewById(R.id.contactGrid)
        contactGrid.layoutManager = GridLayoutManager(this, 2)

        val contacts = loadSelectedContacts()
        contactGrid.adapter = ContactGridAdapter(contacts) { contact ->
            callContact(contact)
        }
        */

    }

    private fun startClock() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val now = Date()
                txtTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
                txtDate.text = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(now)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun monitorBattery() {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                txtBattery.text = "$level%"
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
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
