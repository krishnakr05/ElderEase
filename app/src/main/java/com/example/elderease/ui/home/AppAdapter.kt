package com.example.elderease.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo

class AppAdapter(
    private val allApps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private val filteredApps = mutableListOf<AppInfo>()

    init {
        filteredApps.addAll(allApps)
    }

    fun filter(query: String) {
        filteredApps.clear()
        if (query.isBlank()) {
            filteredApps.addAll(allApps)
        } else {
            filteredApps.addAll(
                allApps.filter {
                    it.label.contains(query, ignoreCase = true)
                }
            )
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.bind(app)
    }

    override fun getItemCount() = filteredApps.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(app: AppInfo) {
            itemView.findViewById<TextView>(R.id.appName).text = app.label
            itemView.findViewById<ImageView>(R.id.appIcon).setImageDrawable(app.icon)

            itemView.setOnClickListener {
                onClick(app)
            }
        }
    }
}

