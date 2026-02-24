package com.example.elderease.ui.allapps

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
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
        val searchView = findViewById<SearchView>(R.id.searchApps)

        recyclerView.layoutManager = GridLayoutManager(this, 3)

        val apps = loadAllApps()
        val adapter = AppAdapter(apps) { app ->
            app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(app.launchIntent)
        }

        recyclerView.adapter = adapter

        /* 🔍 SEARCH LISTENER */
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        /* ❌ CLEAR BUTTON */
        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            adapter.filter("")
            false
        }

        /* 🎤 DUMMY VOICE BUTTON (placeholder) */
        val voiceIconId = searchView.context.resources
            .getIdentifier("android:id/search_voice_btn", null, null)

        val voiceButton = searchView.findViewById<android.view.View>(voiceIconId)
        voiceButton?.setOnClickListener {
            Toast.makeText(
                this,
                "Voice search coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadAllApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)

        val apps = resolveInfos.mapNotNull { resolveInfo ->
            val launchIntent =
                pm.getLaunchIntentForPackage(resolveInfo.activityInfo.packageName)

            launchIntent?.let {
                AppInfo(
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = resolveInfo.loadIcon(pm),
                    launchIntent = launchIntent
                )
            }
        }

        return apps.sortedBy { it.label.lowercase() }
    }

}


