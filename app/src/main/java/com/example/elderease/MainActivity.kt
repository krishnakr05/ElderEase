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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val setupState = SetupState(this)
        val caregiverPrefs = CaregiverPrefs(this)

        when {
            // 1️⃣ Apps not selected
            !setupState.isAppsDone() -> {
                startActivity(
                    Intent(this, SetupAppsActivity::class.java)
                )
            }

            // 2️⃣ Contacts not selected
            !setupState.isContactsDone() -> {
                startActivity(
                    Intent(this, ContactSetupActivity::class.java)
                )
            }

            // 3️⃣ Caregiver PIN not set
            !caregiverPrefs.isPinSet() -> {
                startActivity(
                    Intent(this, CaregiverLoginActivity::class.java)
                        .putExtra(
                            "MODE",
                            CaregiverLoginActivity.MODE_SET
                        )
                )
            }

            // 4️⃣ All done
            else -> {
                startActivity(
                    Intent(this, HomeActivity::class.java)
                )
            }
        }

        finish()
    }
}