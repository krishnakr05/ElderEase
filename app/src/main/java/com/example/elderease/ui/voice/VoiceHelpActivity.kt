package com.example.elderease.ui.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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

class VoiceHelpActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var txtResult: TextView
    private lateinit var btnMic: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnStop: Button
    private lateinit var tts: TextToSpeech

    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_help)

        txtResult = findViewById(R.id.txtResult)
        btnMic = findViewById(R.id.btnMic)
        btnBack = findViewById(R.id.btnBack)
        btnStop = findViewById(R.id.btnStop)

        txtResult.text = "Tap the mic to speak"

        tts = TextToSpeech(this, this)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
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
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // ⭐ START LISTENING
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

        if (!isListening) {
            isListening = true
            txtResult.text = "Listening..."
            speechRecognizer.startListening(speechIntent)
        }
    }

    // ⭐ STOP LISTENING MANUALLY
    private fun stopListening() {
        try {
            speechRecognizer.stopListening()
            speechRecognizer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isListening = false
        txtResult.text = "Tap the mic to speak"
    }

    private fun setupListener() {

        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle) {

                val matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                val spoken = matches?.get(0) ?: ""

                txtResult.text = spoken
                isListening = false

                handleCommand(spoken.lowercase(Locale.getDefault()))
            }

            override fun onError(error: Int) {
                txtResult.text = "Tap the mic to speak"
                isListening = false
            }

            override fun onEndOfSpeech() {
                // Normal behavior — stops automatically
            }

            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
    }

    // ⭐ COMMAND HANDLER
    private fun handleCommand(command: String) {

        when {

            command.contains("open") -> {
                val appName = command.replace("open", "").trim()
                openAnyApp(appName)
            }

            command.contains("call") -> {
                val name = command.replace("call", "").trim()
                callContact(name)
            }

            command.contains("help") -> {
                startActivity(Intent(this, EmergencyActivity::class.java))
            }

            command.contains("stop") -> {
                stopListening()
            }

            else -> speak("I did not understand")
        }
    }

    // ⭐ OPEN APP
    private fun openAnyApp(appName: String) {

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = pm.queryIntentActivities(intent, 0)

        for (app in apps) {

            val label = app.loadLabel(pm).toString().lowercase()

            if (label.contains(appName)) {

                val launchIntent =
                    pm.getLaunchIntentForPackage(app.activityInfo.packageName)

                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(launchIntent)
                return
            }
        }

        speak("App not found")
    }

    // ⭐ CALL CONTACT
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

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        2
                    )
                    return
                }

                val intent = Intent(Intent.ACTION_CALL)
                intent.data = android.net.Uri.parse("tel:$phone")

                startActivity(intent)
                speak("Calling now")
                return
            }
        }

        speak("Contact not found")
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}