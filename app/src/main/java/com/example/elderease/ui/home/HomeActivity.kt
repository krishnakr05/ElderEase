package com.example.elderease.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.elderease.BaseActivity
import com.example.elderease.R
import com.example.elderease.model.AppInfo
import com.example.elderease.ui.setup.SetupAppsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : BaseActivity() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var appAdapter: AppAdapter
    private val apps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.appGrid)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.isNestedScrollingEnabled = false

        appAdapter = AppAdapter(apps) { app ->
            launchApp(app)
        }
        recyclerView.adapter = appAdapter

        refreshApps()

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    speak("Home")
                    vibrate()
                    true
                }

                R.id.nav_settings -> {
                    speakAndRun("Opening settings") {
                        startActivity(Intent(this, SetupAppsActivity::class.java))
                    }
                    true
                }

                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        speak("Home screen")
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

                result.add(
                    AppInfo(
                        label = label,
                        icon = icon,
                        launchIntent = launchIntent
                    )
                )

            } catch (e: PackageManager.NameNotFoundException) {
                Log.d("HomeActivity", "Package not found: $pkg")
            }
        }

        return result
    }

    private fun launchApp(app: AppInfo) {
        speakAndRun("Opening ${app.label}") {
            app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(app.launchIntent)
        }
    }
}