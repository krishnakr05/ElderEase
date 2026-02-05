package com.example.elderease.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val recyclerView: RecyclerView = findViewById(R.id.appGrid)
        // Fixed number of columns for a stable, large-grid layout
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val apps = loadLaunchableApps()
        recyclerView.adapter = AppGridAdapter(apps) { app ->
            launchApp(app)
        }
    }

    /**
     * Builds a list of all launchable apps on the device,
     * using real labels and icons from the PackageManager.
     *
     * This avoids "empty grid" issues when hard-coded package names
     * don't exist on a particular device. You still get predictable
     * behavior because the list is sorted by label.
     */
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

        // Deterministic order: alphabetical by label
        result.sortBy { it.label.lowercase() }

        return result
    }

    /**
     * Centralized place to start an app from the grid.
     */
    private fun launchApp(app: AppInfo) {
        app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(app.launchIntent)
    }
}
