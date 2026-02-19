package com.example.elderease.ui.voice
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.elderease.R
import com.example.elderease.ui.emergency.EmergencyActivity
import java.util.*

class VoiceHelpActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var btnMic: Button
    private lateinit var btnBack: ImageView
    private lateinit var txtResult: TextView
    private lateinit var tts: TextToSpeech

    private var continuousMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_help)

        btnMic = findViewById(R.id.btnMic)
        btnBack = findViewById(R.id.btnBack)
        txtResult = findViewById(R.id.txtResult)

        tts = TextToSpeech(this, this)

        btnBack.setOnClickListener { finish() }

        btnMic.setOnClickListener {
            continuousMode = true
            checkPermissionAndStart()
        }
    }

    // ✅ Malayalam TTS
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("ml")
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // 🎤 Permission
    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceRecognition()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startVoiceRecognition()
            else speak("Microphone permission needed")
        }

    // 🎤 Speech result
    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {

                val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spoken = matches?.get(0) ?: ""

                txtResult.text = spoken

                handleCommand(spoken.lowercase(Locale.getDefault()))

                if (continuousMode) startVoiceRecognition()
            }
        }
    private val requestCallPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) speak("Permission granted, please try again")
            else speak("Call permission denied")
        }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechLauncher.launch(intent)
    }

    // 🧠 Command handler
    private fun handleCommand(command: String) {

        val cleanCommand = command.lowercase(Locale.getDefault())

        when {

            // 📱 OPEN APP
            cleanCommand.contains("open") -> {
                speak("Opening app")
                val appName = cleanCommand.replace("open", "").trim()
                openAnyApp(appName)
            }

            // 📞 CALL CONTACT
            cleanCommand.contains("call") -> {

                val name = cleanCommand
                    .replace("call", "")
                    .replace("please", "")
                    .trim()

                speak("Calling $name")
                callContact(name)
            }


            // 🚨 EMERGENCY
            cleanCommand.contains("emergency") ||
                    cleanCommand.contains("help me") -> {

                speak("Starting emergency")
                startActivity(Intent(this, EmergencyActivity::class.java))
            }

            // 🛑 STOP LISTENING
            cleanCommand.contains("stop") ||
                    cleanCommand.contains("cancel") -> {

                speak("Stopping listening")
                continuousMode = false
            }

            else -> {
                speak("Sorry I did not understand")
            }
        }
    }


    // 📱 Open apps
    private fun openAnyApp(appName: String) {

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = pm.queryIntentActivities(intent, 0)

        for (app in apps) {

            val label = app.loadLabel(pm).toString().lowercase(Locale.getDefault())

            if (label.contains(appName) || appName.contains(label)) {

                val launchIntent =
                    pm.getLaunchIntentForPackage(app.activityInfo.packageName)

                launchIntent?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }

                startActivity(launchIntent)
                return
            }
        }

        speak("App $appName not found")
    }

    // 📞 Improved contact search
    private fun callContact(nameQuery: String) {

        if (nameQuery.isEmpty()) {
            speak("Please say the contact name")
            return
        }

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
            arrayOf("%$nameQuery%"),
            null
        )

        cursor?.use {

            if (it.moveToFirst()) {

                val phoneIndex = it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                val phone = it.getString(phoneIndex)

                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = android.net.Uri.parse("tel:$phone")

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startActivity(callIntent)
                } else {
                    requestCallPermission.launch(Manifest.permission.CALL_PHONE)
                }

                return
            }
        }

        speak("Contact $nameQuery not found")
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
