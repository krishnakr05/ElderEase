package com.example.elderease.ui.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import com.example.elderease.ui.common.ContactRepository

class EmergencyManager(private val context: Context) {

    fun triggerSOS() {

        val numbers = ContactRepository.loadSelectedPhones(context)

        if (numbers.size != 3) {
            Toast.makeText(context,
                "Please set 3 emergency contacts",
                Toast.LENGTH_LONG).show()
            return
        }

        sendSMS(numbers)
        callFirst(numbers[0])
    }

    private fun sendSMS(numbers: List<String>) {
        val smsManager = SmsManager.getDefault()

        for (number in numbers) {
            smsManager.sendTextMessage(
                number,
                null,
                "Emergency! I need help. Please contact me immediately.",
                null,
                null
            )
        }
    }

    private fun callFirst(number: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}