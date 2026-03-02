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
import com.example.elderease.ui.caregiver.CaregiverLoginActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var txtTime: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtBattery: TextView

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var appAdapter: AppAdapter
    private val apps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerApps)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView = findViewById(R.id.recyclerApps)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        appAdapter = AppAdapter(apps) { app ->
            launchApp(app)
        }
        recyclerView.adapter = appAdapter


        appAdapter = AppAdapter(apps) { app ->
            launchApp(app)
        }
        recyclerView.adapter = appAdapter

        refreshApps()   // initial load

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
            val intent = Intent(this, CaregiverLoginActivity::class.java)
            intent.putExtra("MODE", CaregiverLoginActivity.MODE_VERIFY)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnContacts).setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        findViewById<Button>(R.id.btnAllApps).setOnClickListener {
            startActivity(Intent(this, AllAppsActivity::class.java))
        }

        findViewById<TextView>(R.id.txtTitle).text = "ElderEase"
    }

    override fun onResume() {
        super.onResume()
        refreshApps()
    }

    private fun refreshApps() {
        val prefs = getSharedPreferences(
            SetupAppsActivity.PREFS_NAME,
            MODE_PRIVATE
        )

        val packages = prefs
            .getString(SetupAppsActivity.KEY_SELECTED_PACKAGES, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val newApps = loadSelectedApps(packages)

        apps.clear()
        apps.addAll(newApps)
        appAdapter.notifyDataSetChanged()

        Log.d("HomeActivity", "Apps refreshed: ${apps.size}")
        Log.d("HomeActivity", "Saved packages raw: ${prefs.getString(SetupAppsActivity.KEY_SELECTED_PACKAGES, "NULL")}")
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
