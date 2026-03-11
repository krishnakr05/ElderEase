package com.example.elderease.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R

class PreviewAdapter(
    var iconSize: Int,
    var textSize: Float
) : RecyclerView.Adapter<PreviewAdapter.ViewHolder>() {

    // Dummy preview items
    private val labels = listOf("Phone", "Messages", "Camera", "Photos", "Maps", "Weather")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(labels[position % labels.size])
    }

    override fun getItemCount() = 6

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(label: String) {
            val nameView = itemView.findViewById<TextView>(R.id.appName)
            val iconView = itemView.findViewById<ImageView>(R.id.appIcon)

            nameView.text = label
            nameView.textSize = textSize

            val px = (iconSize * itemView.context.resources.displayMetrics.density).toInt()
            val params = iconView.layoutParams
            params.width = px
            params.height = px
            iconView.layoutParams = params

            // Use a generic system icon for preview
            iconView.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }
}