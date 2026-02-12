package com.example.elderease.ui.setup

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.ui.home.HomeActivity

/**
 * First-time setup: user picks which apps appear on the home grid.
 * Saves selection to SharedPreferences and marks setup complete so HomeActivity shows only those apps.
 */
class SetupAppsActivity : ComponentActivity() {

    companion object {
        const val PREFS_NAME = "elderease_setup"
        const val KEY_SETUP_COMPLETE = "is_setup_complete"
        const val KEY_SELECTED_PACKAGES = "selected_app_packages"
    }

    private lateinit var adapter: SetupAppsAdapter
    private val items = mutableListOf<SetupAppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_apps)

        val list: RecyclerView = findViewById(R.id.setupAppList)
        list.layoutManager = LinearLayoutManager(this)

        items.clear()
        items.addAll(loadAllLaunchableApps())
        adapter = SetupAppsAdapter(items) { /* selection changed, no op needed */ }
        list.adapter = adapter

        findViewById<android.widget.Button>(R.id.setupContinue).setOnClickListener {
            saveSelectionAndGoToContacts()
        }
    }

    /**
     * Same source as HomeActivity (PackageManager LAUNCHER), but we keep package name and use SetupAppItem.
     * Excludes this app so the launcher itself is not in the list.
     */
    private fun loadAllLaunchableApps(): List<SetupAppItem> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val result = mutableListOf<SetupAppItem>()
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == packageName) continue
            val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: continue
            val label = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)
            result.add(SetupAppItem(packageName = pkg, label = label, icon = icon, selected = false))
        }
        result.sortBy { it.label.lowercase() }
        return result
    }

    /**
     * Persist selected package names in list order (comma-separated), set setup complete, go to home.
     */
    private fun saveSelectionAndGoToContacts() {
        val selected = items.filter { it.selected }.map { it.packageName }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            .putString(KEY_SELECTED_PACKAGES, selected.joinToString(","))
            .apply()   // ❗ do NOT mark setup complete here

        val intent = Intent(this, ContactSetupActivity::class.java)
        startActivity(intent)
        finish()
    }
}
