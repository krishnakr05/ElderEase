package com.example.elderease.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo

/**
 * Simple grid adapter for the home screen.
 * It only binds already-prepared AppInfo data and forwards click events.
 */
class AppGridAdapter(
    private val apps: List<AppInfo>,
    private val onAppClicked: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconView: ImageView = itemView.findViewById(R.id.appIcon)
        val labelView: TextView = itemView.findViewById(R.id.appLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        holder.iconView.setImageDrawable(app.icon)
        holder.labelView.text = app.label

        holder.itemView.setOnClickListener {
            onAppClicked(app)
        }
    }

    override fun getItemCount(): Int = apps.size
}
