package com.example.elderease.ui.setup

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.data.storage.SetupState
import com.example.elderease.ui.home.HomeActivity

/**
 * First-time setup: user picks which apps appear on the home grid.
 * Saves selection to SharedPreferences and marks setup complete so HomeActivity shows only those apps.
 */
class SetupAppsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "favorite_apps"
        //const val KEY_SETUP_COMPLETE = "is_setup_complete"
        const val KEY_SELECTED_PACKAGES = "selected_app_packages"
    }

    private lateinit var adapter: SetupAppsAdapter
    private val items = mutableListOf<SetupAppItem>()

    private var mode: String = "SETUP"

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.SEND_SMS,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_apps)

        mode = intent.getStringExtra("MODE") ?: "SETUP"

        val list: RecyclerView = findViewById(R.id.setupAppList)
        list.layoutManager = LinearLayoutManager(this)

        items.clear()
        items.addAll(loadAllLaunchableApps())
        adapter = SetupAppsAdapter(items) { /* selection changed, no op needed */ }
        list.adapter = adapter

        if (mode == "EDIT") {
            preloadSelectedApps()
        }

        findViewById<android.widget.Button>(R.id.setupContinue).setOnClickListener {
            saveSelectionAndGoToContacts()
        }

        if (!hasAllPermissions()) {
            requestPermissions(requiredPermissions, 101)
        }
    }

    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {

            val granted = grantResults.all {
                it == android.content.pm.PackageManager.PERMISSION_GRANTED
            }

            if (!granted) {
                Toast.makeText(
                    this,
                    "Permissions are required for ElderEase features",
                    Toast.LENGTH_LONG
                ).show()
            }
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
     * Persist selected package names in list order (comma-separated), set setup complete, go to home.
     */
    private fun saveSelectionAndGoToContacts() {
        val selected = items.filter { it.selected }.map { it.packageName }

        if (selected.isEmpty()) {
            android.widget.Toast.makeText(
                this,
                "Please select at least one app",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            .putString(KEY_SELECTED_PACKAGES, selected.joinToString(","))
            .apply()

        if (mode == "EDIT") {
            // ✅ editing favorites from Settings
            android.widget.Toast.makeText(
                this,
                "Favorite apps updated",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            finish()   // go back to Settings
            return
        }

        SetupState(this).markAppsDone()

        // ✅ first-time setup flow
        startActivity(Intent(this, FavouriteContactSetupActivity::class.java))
        finish()
    }
}
