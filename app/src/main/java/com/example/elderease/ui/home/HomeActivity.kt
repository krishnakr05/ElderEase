package com.example.elderease.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup RecyclerView (App Grid)
        val recyclerView: RecyclerView = findViewById(R.id.appGrid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val apps = loadLaunchableApps()
        recyclerView.adapter = AppGridAdapter(apps) { app ->
            launchApp(app)
        }

        // Setup Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    // Already on home
                    true
                }

                R.id.nav_settings -> {

                    // Open PIN screen in verify mode
                    val intent = Intent(
                        this,
                        com.example.elderease.ui.caregiver.CaregiverLoginActivity::class.java
                    )

                    intent.putExtra("MODE", "VERIFY_SETTINGS")
                    startActivity(intent)

                    true
                }

                else -> false
            }
        }
    }

    // Load all launchable apps
    private fun loadLaunchableApps(): List<AppInfo> {
        val pm = packageManager
        val result = mutableListOf<AppInfo>()

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        for (info in resolveInfos) {
            val activityInfo = info.activityInfo
            val pkg = activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: continue
            val label = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)

            result.add(
                AppInfo(
                    label = label,
                    icon = icon,
                    launchIntent = launchIntent
                )
            )
        }

        // Sort alphabetically
        result.sortBy { it.label.lowercase() }

        return result
    }

    // Launch selected app
    private fun launchApp(app: AppInfo) {
        app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(app.launchIntent)
    }
}
