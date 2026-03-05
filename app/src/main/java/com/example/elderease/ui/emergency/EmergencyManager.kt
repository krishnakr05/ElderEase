package com.example.elderease.ui.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.elderease.ui.common.ContactRepository
import com.google.android.gms.location.*

class EmergencyManager(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun triggerSOS() {

        val numbers = ContactRepository.loadSelectedPhones(context)

        if (numbers.size != 3) {
            Toast.makeText(
                context,
                "Please set 3 emergency contacts",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            sendSMS(numbers, null)
            callFirst(numbers.first())
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        )
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                fusedLocationClient.removeLocationUpdates(this)

                val location = locationResult.lastLocation

                val locationLink = if (location != null) {
                    "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    null
                }

                sendSMS(numbers, locationLink)
                callFirst(numbers.first())
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
    }

    private fun sendSMS(numbers: List<String>, locationLink: String?) {

        val smsManager = SmsManager.getDefault()

        val message = if (locationLink != null) {
            "Emergency! I need help.\nPlease contact me immediately.\n\nMy location:\n$locationLink"
        } else {
            "Emergency! I need help.\nPlease contact me immediately."
        }

        for (number in numbers) {
            smsManager.sendTextMessage(
                number,
                null,
                message,
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
