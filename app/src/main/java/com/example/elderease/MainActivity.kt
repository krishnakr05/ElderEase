package com.example.elderease

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.data.storage.CaregiverPrefs
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.common.ContactRepository
import com.example.elderease.ui.home.HomeActivity
import com.example.elderease.ui.setup.CaregiverSetupActivity
import com.example.elderease.ui.setup.ContactSetupActivity
import com.example.elderease.ui.setup.FavouriteContactSetupActivity
import com.example.elderease.ui.setup.SetupAppsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val caregiverPrefs = CaregiverPrefs(this)

        val selectedPackages = getSharedPreferences(
            SetupAppsActivity.PREFS_NAME,
            MODE_PRIVATE
        ).getString(SetupAppsActivity.KEY_SELECTED_PACKAGES, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val favouriteContacts = getSharedPreferences(
            "elder_favourites",
            MODE_PRIVATE
        ).getString("fav_contacts", "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val emergencyContacts = ContactRepository.loadSelectedPhones(this)

        val elderPrefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val caregiverSetupDone = elderPrefs.contains("caregiver_enabled")

        when {
            selectedPackages.isEmpty() -> {
                startActivity(Intent(this, SetupAppsActivity::class.java))
            }

            favouriteContacts.isEmpty() -> {
                startActivity(Intent(this, FavouriteContactSetupActivity::class.java))
            }

            emergencyContacts.size != 3 -> {
                startActivity(Intent(this, ContactSetupActivity::class.java))
            }

            !caregiverSetupDone -> {
                startActivity(Intent(this, CaregiverSetupActivity::class.java))
            }

            else -> {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }

        finish()
    }
}