package com.example.elderease.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.allapps.AllAppsActivity
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.contacts.ContactsActivity
import com.example.elderease.ui.emergency.EmergencyActivity
import com.example.elderease.ui.settings.SettingsActivity
import com.example.elderease.ui.setup.SetupAppsActivity
import com.example.elderease.ui.voice.VoiceHelpActivity
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var txtTime: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtBattery: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var appAdapter: AppAdapter
    private val apps = mutableListOf<AppInfo>()

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // TTS
        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        // All Apps button
        val btnAllApps = findViewById<Button>(R.id.btnAllApps)
        setPressEffect(btnAllApps, "Opening all apps") {
            startActivity(Intent(this, AllAppsActivity::class.java))
        }

        refreshAllAppsButton()

        // Recycler
        recyclerView = findViewById(R.id.recyclerApps)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        appAdapter = AppAdapter(
            apps = apps,
            onClick = { app -> launchApp(app) },
            onVibrate = { vibrateTap() }
        )

        recyclerView.adapter = appAdapter

        refreshApps()

        // Top info
        txtTime = findViewById(R.id.txtTime)
        txtDate = findViewById(R.id.txtDate)
        txtBattery = findViewById(R.id.txtBattery)

        startClock()
        monitorBattery()

        // Help
        val btnHelp = findViewById<View>(R.id.btnHelp)
        setPressEffect(btnHelp, "Opening help") {
            startActivity(Intent(this, VoiceHelpActivity::class.java))
        }

        // Emergency
        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        setPressEffect(btnEmergency, "Opening emergency") {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        // Settings (MERGED LOGIC ✅)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        setPressEffect(btnSettings, "Opening settings") {

            val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
            val caregiverEnabled = prefs.getBoolean("caregiver_enabled", false)

            if (caregiverEnabled) {
                val intent = Intent(this, CaregiverLoginActivity::class.java)
                intent.putExtra(
                    CaregiverLoginActivity.EXTRA_MODE,
                    CaregiverLoginActivity.MODE_VERIFY
                )
                startActivity(intent)
            } else {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        // Contacts
        val btnContacts = findViewById<Button>(R.id.btnContacts)
        setPressEffect(btnContacts, "Opening contacts") {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        findViewById<TextView>(R.id.txtTitle).text = "ElderEase"
    }

    // =========================
    // FEEDBACK SYSTEM
    // =========================

    private fun isVoiceFeedbackEnabled(): Boolean {
        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        return prefs.getBoolean("voice_enabled", false)
    }

    private fun isVibrationFeedbackEnabled(): Boolean {
        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        return prefs.getBoolean("vibration_enabled", false)
    }

    private fun vibrateTap() {
        if (!isVibrationFeedbackEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                vibrator.vibrate(40)
            }
        }
    }

    private fun speakFeedback(text: String) {
        if (isVoiceFeedbackEnabled()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun setPressEffect(view: View, spokenText: String? = null, action: () -> Unit) {
        view.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).start()
                    vibrateTap()
                }

                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                    spokenText?.let { speakFeedback(it) }
                    action()
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                }
            }
            true
        }
    }

    // =========================
    // LIFECYCLE
    // =========================

    override fun onResume() {
        super.onResume()
        refreshApps()
        refreshAllAppsButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    // =========================
    // UI UPDATES
    // =========================

    private fun refreshApps() {
        val prefs = getSharedPreferences(SetupAppsActivity.PREFS_NAME, MODE_PRIVATE)
        val settingsPrefs = getSharedPreferences("elder_settings", MODE_PRIVATE)

        val packages = prefs
            .getString(SetupAppsActivity.KEY_SELECTED_PACKAGES, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val newApps = loadSelectedApps(packages)

        apps.clear()
        apps.addAll(newApps)

        val grid = settingsPrefs.getInt("home_grid", 2)
        val iconSize = settingsPrefs.getInt("home_icon_size", 96)
        val textSize = settingsPrefs.getFloat("home_text_size", 18f)

        recyclerView.layoutManager = GridLayoutManager(this, grid)

        appAdapter.iconSize = iconSize
        appAdapter.textSize = textSize
        appAdapter.vibrationEnabled = isVibrationFeedbackEnabled()

        appAdapter.notifyDataSetChanged()
    }

    private fun refreshAllAppsButton() {
        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val showAllApps = prefs.getBoolean("show_all_apps", true)

        val btnAllApps = findViewById<Button>(R.id.btnAllApps)
        btnAllApps.visibility = if (showAllApps) View.VISIBLE else View.GONE
    }

    private fun startClock() {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                val now = Date()
                txtTime.text =
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
                txtDate.text =
                    SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(now)

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }

    private fun monitorBattery() {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level =
                    intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                txtBattery.text = "$level%"
            }
        }

        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    // =========================
    // APP LOADING
    // =========================

    private fun loadSelectedApps(packageNames: List<String>): List<AppInfo> {
        val pm = packageManager
        val result = mutableListOf<AppInfo>()

        for (pkg in packageNames) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: continue
                val appInfo = pm.getApplicationInfo(pkg, 0)

                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)

                result.add(AppInfo(label, icon, launchIntent))
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }

        return result
    }

    private fun launchApp(app: AppInfo) {
        if (isVoiceFeedbackEnabled()) {
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
}