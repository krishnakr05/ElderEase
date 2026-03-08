package com.example.elderease.ui.emergency

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R

class EmergencyContactAdapter(
    private val contacts: List<Pair<String, String>>
) : RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: TextView = view.findViewById(R.id.txtAvatar)
        val name: TextView = view.findViewById(R.id.txtName)
        val phone: TextView = view.findViewById(R.id.txtPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val contact = contacts[position]

        holder.name.text = contact.first
        holder.phone.text = contact.second
        holder.avatar.text = contact.first.first().toString()
    }

    override fun getItemCount(): Int = contacts.size


}
