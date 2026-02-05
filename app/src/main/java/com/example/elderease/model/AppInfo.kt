package com.example.elderease.model

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Minimal data needed to render and launch an app from the home grid.
 */
data class AppInfo(
    val label: String,
    val icon: Drawable,
    val launchIntent: Intent
)
