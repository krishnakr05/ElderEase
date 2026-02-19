package com.example.elderease.ui.setup

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R

/**
 * Item for setup list: app identity + selection state.
 * Why: Adapter needs package name to save and mutable selected for checkbox.
 */
data class SetupAppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    var selected: Boolean
)

/**
 * RecyclerView adapter for the setup app list.
 * Binds icon, label, checkbox; toggles item.selected on checkbox click so Continue can read selection.
 */
class SetupAppsAdapter(
    private val items: MutableList<SetupAppItem>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<SetupAppsAdapter.SetupViewHolder>() {

    class SetupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.setupItemCheck)
        val iconView: ImageView = itemView.findViewById(R.id.setupItemIcon)
        val labelView: TextView = itemView.findViewById(R.id.setupItemLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setup_app, parent, false)
        return SetupViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetupViewHolder, position: Int) {
        val item = items[position]
        holder.iconView.setImageDrawable(item.icon)
        holder.labelView.text = item.label
        holder.checkBox.isChecked = item.selected
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.selected = isChecked
            onSelectionChanged()
        }
    }

    override fun getItemCount(): Int = items.size
}
