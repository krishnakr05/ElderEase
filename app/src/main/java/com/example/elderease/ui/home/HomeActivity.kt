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
import android.speech.tts.TextToSpeech
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

class HomeActivity : AppCompatActivity() {

    private lateinit var txtTime: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtBattery: TextView

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var appAdapter: AppAdapter
    private val apps = mutableListOf<AppInfo>()

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        // Text To Speech initialization
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val showAllApps = prefs.getBoolean("show_all_apps", true)
        val btnAllApps = findViewById<Button>(R.id.btnAllApps)

        setPressEffect(btnAllApps) {
            startActivity(Intent(this, AllAppsActivity::class.java))
        }

        refreshAllAppsButton()

        recyclerView = findViewById(R.id.recyclerApps)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        appAdapter = AppAdapter(apps) { app ->
            launchApp(app)
        }

        recyclerView.adapter = appAdapter

        refreshApps()

        txtTime = findViewById(R.id.txtTime)
        txtDate = findViewById(R.id.txtDate)
        txtBattery = findViewById(R.id.txtBattery)

        startClock()
        monitorBattery()

        val btnHelp = findViewById<android.widget.LinearLayout>(R.id.btnHelp)
        setPressEffect(btnHelp) {
            startActivity(Intent(this, VoiceHelpActivity::class.java))
        }

        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        setPressEffect(btnEmergency) {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        val btnSettings = findViewById<Button>(R.id.btnSettings)
        setPressEffect(btnSettings) {
            val intent = Intent(this, CaregiverLoginActivity::class.java)
            intent.putExtra("MODE", CaregiverLoginActivity.MODE_VERIFY)
            startActivity(intent)
        }

        val btnContacts = findViewById<Button>(R.id.btnContacts)
        setPressEffect(btnContacts) {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        findViewById<TextView>(R.id.txtTitle).text = "ElderEase"
    }

    private fun setPressEffect(view: View, action: () -> Unit) {
        view.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).start()
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }

                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                    action()
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        refreshApps()
        refreshAllAppsButton()
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

    private fun refreshAllAppsButton() {

        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val showAllApps = prefs.getBoolean("show_all_apps", true)

        val btnAllApps = findViewById<Button>(R.id.btnAllApps)

        btnAllApps.visibility =
            if (showAllApps) android.view.View.VISIBLE else android.view.View.GONE
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

        // Speak app name
        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val voiceEnabled = prefs.getBoolean("voice_feedback", false)

        if (voiceEnabled) {
            tts.speak("Opening ${app.label}", TextToSpeech.QUEUE_FLUSH, null, null)
        }

        app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(app.launchIntent)
    }

    private fun callContact(contact: ContactInfo) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phone}")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}