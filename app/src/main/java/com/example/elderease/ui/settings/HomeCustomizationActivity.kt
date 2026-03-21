package com.example.elderease.ui.settings

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elderease.R
import com.example.elderease.utils.KEY_HOME_GRID
import com.example.elderease.utils.KEY_HOME_ICON
import com.example.elderease.utils.KEY_HOME_TEXT
import com.example.elderease.utils.PREFS_SETTINGS

class HomeCustomizationActivity : AppCompatActivity() {

    private lateinit var sliderGrid: SeekBar
    private lateinit var sliderIcon: SeekBar
    private lateinit var sliderText: SeekBar
    private lateinit var previewRecycler: RecyclerView
    private lateinit var labelGrid: TextView
    private lateinit var labelIcon: TextView
    private lateinit var labelText: TextView

    private lateinit var previewAdapter: PreviewAdapter

    private var grid = 2
    private var iconSize = 96
    private var textSize = 18f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_customization)

        val prefs = getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)

        grid = prefs.getInt(KEY_HOME_GRID, 2)
        iconSize = prefs.getInt(KEY_HOME_ICON, 96)
        textSize = prefs.getFloat(KEY_HOME_TEXT, 18f)

        sliderGrid = findViewById(R.id.sliderGrid)
        sliderIcon = findViewById(R.id.sliderIcon)
        sliderText = findViewById(R.id.sliderText)
        previewRecycler = findViewById(R.id.previewRecycler)
        labelGrid = findViewById(R.id.labelGridValue)
        labelIcon = findViewById(R.id.labelIconValue)
        labelText = findViewById(R.id.labelTextValue)

        // Grid: 1–4 columns → progress 0–3
        sliderGrid.max = 3
        sliderGrid.progress = grid - 1

        // Icon: 48dp–144dp → progress 0–96
        sliderIcon.max = 96
        sliderIcon.progress = iconSize - 48

        // Text: 12sp–30sp → progress 0–18
        sliderText.max = 18
        sliderText.progress = (textSize - 12).toInt()

        updateLabels()

        previewAdapter = PreviewAdapter(iconSize, textSize)
        previewRecycler.layoutManager = GridLayoutManager(this, grid)
        previewRecycler.adapter = previewAdapter

        setupListeners(prefs)

        findViewById<android.widget.Button>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun setupListeners(prefs: android.content.SharedPreferences) {
        sliderGrid.setOnSeekBarChangeListener(simpleListener {
            grid = sliderGrid.progress + 1
            prefs.edit().putInt(KEY_HOME_GRID, grid).apply()
            updateLabels()
            updatePreview()
        })

        sliderIcon.setOnSeekBarChangeListener(simpleListener {
            iconSize = sliderIcon.progress + 48
            prefs.edit().putInt(KEY_HOME_ICON, iconSize).apply()
            updateLabels()
            updatePreview()
        })

        sliderText.setOnSeekBarChangeListener(simpleListener {
            textSize = sliderText.progress + 12f
            prefs.edit().putFloat(KEY_HOME_TEXT, textSize).apply()
            updateLabels()
            updatePreview()
        })
    }

    private fun updateLabels() {
        labelGrid.text = "$grid col"
        labelIcon.text = "${iconSize}dp"
        labelText.text = "${textSize.toInt()}sp"
    }

    private fun updatePreview() {
        previewRecycler.layoutManager = GridLayoutManager(this, grid)
        previewAdapter.iconSize = iconSize
        previewAdapter.textSize = textSize
        previewAdapter.notifyDataSetChanged()
    }

    private fun simpleListener(action: () -> Unit) =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                action()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
}