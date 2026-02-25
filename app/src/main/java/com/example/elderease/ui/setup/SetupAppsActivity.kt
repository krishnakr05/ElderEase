package com.example.elderease.ui.setup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R

class SetupAppsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "elderease_setup"
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

        adapter = SetupAppsAdapter(items) { }
        list.adapter = adapter

        findViewById<Button>(R.id.setupContinue).setOnClickListener {
            saveSelectionAndGoToFavouriteContacts()
        }
    }

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

    private fun saveSelectionAndGoToFavouriteContacts() {

        val selected = items.filter { it.selected }.map { it.packageName }

        if (selected.isEmpty()) {
            Toast.makeText(
                this,
                "Please select at least one favourite app",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_PACKAGES, selected.joinToString(","))
            .apply()

        // 🔥 IMPORTANT → Move to Favourite Contact setup
        startActivity(Intent(this, FavouriteContactSetupActivity::class.java))
        finish()
    }
}