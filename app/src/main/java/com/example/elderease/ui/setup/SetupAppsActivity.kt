package com.example.elderease.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.data.storage.SetupState
import android.widget.Toast

/**
 * First-time setup: user picks which apps appear on the home grid.
 * Saves selection to SharedPreferences and marks setup complete.
 */
class SetupAppsActivity : ComponentActivity() {

    companion object {
        const val PREFS_NAME = "favorite_apps"
        const val KEY_SELECTED_PACKAGES = "selected_app_packages"
    }

    private lateinit var adapter: SetupAppsAdapter
    private val items = mutableListOf<SetupAppItem>()

    private var mode: String = "SETUP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_apps)

        mode = intent.getStringExtra("MODE") ?: "SETUP"

        val list: RecyclerView = findViewById(R.id.setupAppList)
        list.layoutManager = LinearLayoutManager(this)

        items.clear()
        items.addAll(loadAllLaunchableApps())

        adapter = SetupAppsAdapter(items) { }
        list.adapter = adapter

        if (mode == "EDIT") {
            preloadSelectedApps()
        }

        findViewById<android.widget.Button>(R.id.setupContinue)
            .setOnClickListener {
                saveSelectionAndGoToContacts()
            }
    }

    /**
     * Loads all launchable apps using launcher intent query.
     */
    private fun loadAllLaunchableApps(): List<SetupAppItem> {

        val pm = packageManager

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        val result = mutableListOf<SetupAppItem>()
        val seenPackages = HashSet<String>()

        for (info in resolveInfos) {

            val pkg = info.activityInfo.packageName

            // Skip ElderEase itself
            if (pkg == packageName) continue

            // Avoid duplicate apps
            if (!seenPackages.add(pkg)) continue

            val label = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)

            result.add(
                SetupAppItem(
                    packageName = pkg,
                    label = label,
                    icon = icon,
                    selected = false
                )
            )
        }

        result.sortBy { it.label.lowercase() }

        return result
    }

    /**
     * Preload saved favorite apps when editing from Settings.
     */
    private fun preloadSelectedApps() {

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val saved =
            prefs.getString(KEY_SELECTED_PACKAGES, "")
                ?.split(",")
                ?.toSet() ?: emptySet()

        items.forEach { item ->
            if (saved.contains(item.packageName)) {
                item.selected = true
            }
        }

        adapter.notifyDataSetChanged()
    }

    /**
     * Save selected apps and continue setup flow.
     */
    private fun saveSelectionAndGoToContacts() {

        val selected = items
            .filter { it.selected }
            .map { it.packageName }

        if (selected.isEmpty()) {
            Toast.makeText(
                this,
                "Please select at least one app",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(
                KEY_SELECTED_PACKAGES,
                selected.joinToString(",")
            )
            .apply()

        if (mode == "EDIT") {

            Toast.makeText(
                this,
                "Favorite apps updated",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        SetupState(this).markAppsDone()

        // Continue setup flow
        startActivity(Intent(this, ContactSetupActivity::class.java))
        finish()
    }
}