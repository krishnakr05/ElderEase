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
import androidx.recyclerview.widget.GridLayoutManager
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
import android.widget.Button
import android.widget.TextView
import com.example.elderease.ui.contacts.ContactsActivity
import com.example.elderease.ui.allapps.AllAppsActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var txtTime: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtBattery: TextView

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
        recyclerView.layoutManager = GridLayoutManager(this, 2)
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

        findViewById<Button>(R.id.btnHelp).setOnClickListener {
            startActivity(Intent(this, VoiceHelpActivity::class.java))
        }

        findViewById<Button>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnAllApps).setOnClickListener {
            startActivity(Intent(this, com.example.elderease.ui.allapps.AllAppsActivity::class.java))
        }

        findViewById<Button>(R.id.btnManual).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnContacts).setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        // ⭐ NEW FEATURE — VIEW ALL APPS
        findViewById<Button>(R.id.btnAllApps).setOnClickListener {
            startActivity(Intent(this, AllAppsActivity::class.java))
        }

        findViewById<TextView>(R.id.txtTitle).text = "ElderEase"
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
            }
        }
        return result
    }

    private fun launchApp(app: AppInfo) {
        app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(app.launchIntent)
    }

    private fun callContact(contact: ContactInfo) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phone}")
        }
        startActivity(intent)
    }
}
