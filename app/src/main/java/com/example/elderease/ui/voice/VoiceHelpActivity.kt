package com.example.elderease.ui.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.*
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.elderease.R
import com.example.elderease.ui.emergency.EmergencyActivity
import java.util.*
import android.hardware.camera2.CameraManager
import android.content.Context
import android.media.AudioManager
import android.provider.Telephony
import android.database.Cursor

class VoiceHelpActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var txtResult: TextView
    private lateinit var btnMic: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnStop: Button
    private lateinit var tts: TextToSpeech
    private var isFlashOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_help)

        txtResult = findViewById(R.id.txtResult)
        btnMic = findViewById(R.id.btnMic)
        btnBack = findViewById(R.id.btnBack)
        btnStop = findViewById(R.id.btnStop)

        tts = TextToSpeech(this, this)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // ⭐ Multilingual speech recognition (English + regional automatically)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // allow system to detect language automatically
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        btnBack.setOnClickListener { finish() }
        btnMic.setOnClickListener { startListening() }
        btnStop.setOnClickListener { stopListening() }

        setupListener()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    private fun speak(text: String) {

        val prefs = getSharedPreferences("elder_settings", MODE_PRIVATE)
        val voiceEnabled = prefs.getBoolean("voice_feedback", false)

        if (!voiceEnabled) return

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun startListening() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
            return
        }

        txtResult.text = "Listening..."
        speechRecognizer.startListening(speechIntent)
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        txtResult.text = "Tap the microphone to speak"
    }

    private fun setupListener() {

        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle) {

                val matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                val spoken = matches?.get(0) ?: ""

                txtResult.text = spoken

                handleCommand(spoken.lowercase(Locale.getDefault()))
            }

            override fun onError(error: Int) {
                txtResult.text = "Try again"
            }

            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
    }

    private fun handleCommand(command: String) {

        var clean = command.trim()

        val actionWords = listOf("open", "start", "launch", "please", "the")
        actionWords.forEach {
            clean = clean.replace(it, "").trim()
        }

        // Emergency
        if (clean.contains("help") ||
            clean.contains("emergency") ||
            clean.contains("danger") ||
            clean.contains("i fell")
        ) {
            speak("Opening emergency screen")
            startActivity(Intent(this, EmergencyActivity::class.java))
            return
        }

        // Stop listening
        if (clean.contains("stop")) {
            stopListening()
            return
        }

        // Read SMS
        if (clean.contains("message") || clean.contains("sms")) {
            readMessages()
            return
        }

        // Flashlight
        if (clean.contains("flashlight") || clean.contains("torch")) {

            if (clean.contains("off")) {
                toggleFlashlight(false)
            } else {
                toggleFlashlight(true)
            }

            return
        }

        // Volume
        if (clean.contains("volume") ||
            clean.contains("sound") ||
            clean.contains("louder")
        ) {

            when {

                clean.contains("up") ||
                        clean.contains("increase") ||
                        clean.contains("louder") -> {
                    changeVolume("up")
                }

                clean.contains("down") ||
                        clean.contains("decrease") -> {
                    changeVolume("down")
                }

                clean.contains("max") ||
                        clean.contains("maximum") -> {
                    changeVolume("max")
                }

                clean.contains("mute") -> {
                    changeVolume("mute")
                }
            }

            return
        }

        // Call command
        if (clean.startsWith("call ")) {
            val name = clean.removePrefix("call ").trim()
            callContact(name)
            return
        }

        if (tryCallContact(clean)) return

        if (openAppByName(clean)) return

        speak("I did not understand. Please try again.")
    }

    private fun tryCallContact(nameQuery: String): Boolean {

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$nameQuery%"),
            null
        )

        cursor?.use {

            if (it.moveToFirst()) {

                val phoneIndex = it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                val phone = it.getString(phoneIndex)

                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phone")

                speak("Calling $nameQuery")
                startActivity(intent)
                return true
            }
        }

        return false
    }

    private fun readMessages() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                2
            )
            return
        }

        val cursor: Cursor? = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            null,
            null,
            null,
            "date DESC"
        )

        cursor?.use {

            if (it.moveToFirst()) {

                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)

                val message = it.getString(bodyIndex)
                val sender = it.getString(addressIndex)

                val speakText = "Message from $sender. $message"

                txtResult.text = speakText
                speak(speakText)

            } else {

                speak("You have no messages")

            }
        }
    }

    private fun toggleFlashlight(turnOn: Boolean) {

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, turnOn)

            isFlashOn = turnOn

            if (turnOn) {
                speak("Flashlight turned on")
            } else {
                speak("Flashlight turned off")
            }

        } catch (e: Exception) {
            speak("Unable to control flashlight")
        }
    }

    private fun changeVolume(action: String) {

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        when (action) {

            "up" -> {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                speak("Increasing volume")
            }

            "down" -> {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                speak("Decreasing volume")
            }

            "max" -> {
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
                speak("Volume set to maximum")
            }

            "mute" -> {
                audioManager.adjustVolume(AudioManager.ADJUST_MUTE, 0)
                speak("Volume muted")
            }
        }
    }

    private fun openAppByName(appName: String): Boolean {

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = pm.queryIntentActivities(intent, 0)

        for (app in apps) {

            val label = app.loadLabel(pm).toString().lowercase()

            if (label.contains(appName)) {

                val launchIntent =
                    pm.getLaunchIntentForPackage(app.activityInfo.packageName)

                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                speak("Opening $label")
                startActivity(launchIntent)
                return true
            }
        }

        return false
    }

    private fun callContact(name: String) {
        tryCallContact(name)
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}