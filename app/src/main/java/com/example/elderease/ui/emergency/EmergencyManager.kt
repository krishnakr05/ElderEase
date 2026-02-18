package com.example.elderease.ui.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Toast

class EmergencyManager(private val context: Context) {

    private val prefs = EmergencyPrefs(context)
    private val handler = Handler(Looper.getMainLooper())

    fun triggerSOS() {
        val numbers = prefs.getEmergencyNumbers()

        sendSMSAll(numbers)
        startCallSequence(numbers)

        Toast.makeText(context, "Emergency Alert Sent", Toast.LENGTH_SHORT).show()
    }

    private fun sendSMSAll(numbers: List<String>) {
        val smsManager = SmsManager.getDefault()

        for (number in numbers) {
            if (number.isNotEmpty()) {
                smsManager.sendTextMessage(
                    number,
                    null,
                    "Emergency! Please help.",
                    null,
                    null
                )
            }
        }
    }

    private fun startCallSequence(numbers: List<String>) {
        if (numbers.isEmpty()) return

        // Call first
        callNumber(numbers[0])

        // After 20 sec, call second
        if (numbers.size > 1 && numbers[1].isNotEmpty()) {
            handler.postDelayed({
                endCallIfPossible()
                callNumber(numbers[1])
            }, 20000)
        }

        // After 40 sec, call third
        if (numbers.size > 2 && numbers[2].isNotEmpty()) {
            handler.postDelayed({
                endCallIfPossible()
                callNumber(numbers[2])
            }, 40000)
        }
    }

    private fun callNumber(number: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    // This opens dialer screen to simulate call end
    private fun endCallIfPossible() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
