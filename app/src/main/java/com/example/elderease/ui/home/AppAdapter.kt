package com.example.elderease.ui.home

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.model.AppInfo

class AppAdapter(
    private val apps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)

        fun bind(app: AppInfo) {

            appName.text = app.label
            appIcon.setImageDrawable(app.icon)

            itemView.setOnTouchListener { v, event ->

                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        v.animate()
                            .scaleX(0.93f)
                            .scaleY(0.93f)
                            .setDuration(80)
                            .start()

                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }

                    MotionEvent.ACTION_UP -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start()

                        onClick(app)
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start()
                    }
                }

                true
            }
        }
    }
}