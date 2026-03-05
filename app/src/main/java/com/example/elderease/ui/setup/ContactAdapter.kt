package com.example.elderease.ui.setup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.ContactInfo
import com.example.elderease.ui.common.ContactRepository

class ContactAdapter(
    private val contacts: List<ContactInfo>,
    private val onSelectionChanged: (ContactInfo, Boolean) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.contactName)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]

        holder.name.text = contact.name

        // 🔹 Prevent RecyclerView reuse issues
        holder.checkBox.setOnCheckedChangeListener(null)

        // 🔹 Bind UI to data
        holder.checkBox.isChecked = contact.isSelected

        // 🔹 Notify selection change
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            contact.isSelected = checked
            onSelectionChanged(contact, checked)
        }
    }


    override fun getItemCount(): Int = contacts.size
}
