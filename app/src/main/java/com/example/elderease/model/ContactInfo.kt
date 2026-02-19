package com.example.elderease.model

data class ContactInfo(
    val id: String,
    val name: String,
    val phone: String,
    var isSelected: Boolean = false
)
