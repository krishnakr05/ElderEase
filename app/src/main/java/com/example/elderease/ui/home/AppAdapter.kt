package com.example.elderease.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R

class AppAdapter(
    private val apps: List<android.content.pm.ResolveInfo>,
    private val packageManager: PackageManager
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)

        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        val label = app.loadLabel(packageManager)
        val icon = app.loadIcon(packageManager)

        holder.name.text = label
        holder.icon.setImageDrawable(icon)

        holder.itemView.setOnClickListener {
            val launchIntent =
                packageManager.getLaunchIntentForPackage(app.activityInfo.packageName)
            if (launchIntent != null) {
                holder.itemView.context.startActivity(launchIntent)
            }
        }
    }

    override fun getItemCount(): Int = apps.size
}
