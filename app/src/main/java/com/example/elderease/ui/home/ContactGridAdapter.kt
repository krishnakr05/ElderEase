package com.example.elderease.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.ContactInfo

/**
 * Grid adapter for favorite contacts on the home screen.
 * Separate from the apps adapter to keep responsibilities clear.
 */
class ContactGridAdapter(
    private val contacts: List<ContactInfo>,
    private val onContactClicked: (ContactInfo) -> Unit
) : RecyclerView.Adapter<ContactGridAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarView: TextView = itemView.findViewById(R.id.contactAvatar)
        val nameView: TextView = itemView.findViewById(R.id.contactName)
        val phoneView: TextView = itemView.findViewById(R.id.contactPhone)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_grid, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        holder.nameView.text = contact.name
        holder.phoneView.text = contact.phone

        // Circular avatar letter
        holder.avatarView.text =
            contact.name.trim().firstOrNull()?.uppercase() ?: "?"

        holder.itemView.setOnClickListener {
            onContactClicked(contact)
        }
    }



    override fun getItemCount(): Int = contacts.size
}

