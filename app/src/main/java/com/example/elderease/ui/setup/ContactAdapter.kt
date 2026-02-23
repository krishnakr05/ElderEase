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
    private val context: Context
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

        // 🔹remove old listener
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            contact.isSelected = checked
            ContactRepository.saveSelectedContacts(context, contacts)
        }


        // 🔹 bind UI to data
        holder.checkBox.isChecked = contact.isSelected

        // 🔹 update data from UI
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            contact.isSelected = checked
        }
    }


    override fun getItemCount(): Int = contacts.size
}
