package com.example.elderease.ui.allapps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo
import com.example.elderease.ui.home.AppAdapter

class AllAppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAllApps)
        val btnBackHome = findViewById<Button>(R.id.btnBackHome)
        val btnHomeBottom = findViewById<Button>(R.id.btnHomeBottom)

        recyclerView.layoutManager = GridLayoutManager(this, 3)

        val apps = loadAllApps()

        recyclerView.adapter = AppAdapter(apps) { app ->
            app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(app.launchIntent)
        }

        // Top right Home button
        btnBackHome.setOnClickListener {
            finish()
        }

        // Bottom Home button
        btnHomeBottom.setOnClickListener {
            finish()
        }
    }

    private fun loadAllApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = pm.queryIntentActivities(intent, 0)

        val apps = resolveInfos.mapNotNull {

            val launchIntent = pm.getLaunchIntentForPackage(it.activityInfo.packageName)
                ?: return@mapNotNull null   // prevents crash

            val label = it.loadLabel(pm).toString()
            val icon = it.loadIcon(pm)

            AppInfo(label, icon, launchIntent)
        }

        return apps.sortedBy { it.label.lowercase() }
    }
}
