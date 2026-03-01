package com.example.elderease

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elderease.data.storage.CaregiverPrefs
import com.example.elderease.ui.caregiver.CaregiverLoginActivity
import com.example.elderease.ui.home.HomeActivity
import com.example.elderease.data.storage.SetupState
import com.example.elderease.ui.setup.SetupAppsActivity
import com.example.elderease.ui.setup.ContactSetupActivity
import com.example.elderease.ui.setup.FavouriteContactSetupActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val setupState = SetupState(this)
        val caregiverPrefs = CaregiverPrefs(this)

        when {
            // 1️⃣ Apps
            !setupState.isAppsDone() -> {
                startActivity(Intent(this, SetupAppsActivity::class.java))
            }

            // 2️⃣ Favourite Contacts (MANDATORY)
            !setupState.isFavouriteContactsDone() -> {
                startActivity(Intent(this, FavouriteContactSetupActivity::class.java))
            }

            // 3️⃣ SOS Contacts
            !setupState.isContactsDone() -> {
                startActivity(Intent(this, ContactSetupActivity::class.java))
            }

            // 4️⃣ Caregiver PIN
            !setupState.isPinDone() -> {
                startActivity(
                    Intent(this, CaregiverLoginActivity::class.java)
                        .putExtra(
                            CaregiverLoginActivity.EXTRA_MODE,
                            CaregiverLoginActivity.MODE_SET
                        )
                )
            }

            // 5️⃣ Home
            else -> {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }

        finish()
    }
}