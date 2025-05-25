package com.kaustubh.musicplayer.fragments

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.player.MusicPlayerManager

class EqualizerFragment : Fragment() {
    
    private lateinit var equalizerSwitch: SwitchMaterial
    private lateinit var presetSpinner: Spinner
    private lateinit var equalizerContainer: LinearLayout
    private lateinit var bassBoostSwitch: SwitchMaterial
    private lateinit var bassBoostSeekBar: SeekBar
    private lateinit var virtualizationSwitch: SwitchMaterial
    private lateinit var virtualizationSeekBar: SeekBar
    
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_equalizer, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupAudioEffects()
        setupControls()
    }
    
    private fun initViews(view: View) {
        equalizerSwitch = view.findViewById(R.id.equalizer_switch)
        presetSpinner = view.findViewById(R.id.preset_spinner)
        equalizerContainer = view.findViewById(R.id.equalizer_container)
        bassBoostSwitch = view.findViewById(R.id.bass_boost_switch)
        bassBoostSeekBar = view.findViewById(R.id.bass_boost_seekbar)
        virtualizationSwitch = view.findViewById(R.id.virtualization_switch)
        virtualizationSeekBar = view.findViewById(R.id.virtualization_seekbar)
    }
    
    private fun setupAudioEffects() {
        try {
            val playerManager = MusicPlayerManager.getInstance(requireContext())
            val audioSessionId = playerManager.getAudioSessionId()
            
            // Initialize Equalizer
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = false
            }
            
            // Initialize Bass Boost
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = false
            }
            
            // Initialize Virtualizer
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = false
            }
            
            setupEqualizerBands()
            setupPresets()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupEqualizerBands() {
        equalizer?.let { eq ->
            val numberOfBands = eq.numberOfBands
            
            for (i in 0 until numberOfBands) {
                val bandLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setPadding(8, 0, 8, 0)
                }
                
                // Frequency label
                val freqRange = eq.getCenterFreq(i.toShort())
                val frequency = if (freqRange < 1000) {
                    "${freqRange}Hz"
                } else {
                    "${freqRange / 1000}kHz"
                }
                
                val freqLabel = TextView(context).apply {
                    text = frequency
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    gravity = android.view.Gravity.CENTER
                }
                
                // Gain label
                val gainLabel = TextView(context).apply {
                    text = "0dB"
                    textSize = 10f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    gravity = android.view.Gravity.CENTER
                }
                
                // Vertical SeekBar (rotated)
                val seekBar = SeekBar(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        200
                    )
                    rotation = -90f
                    max = eq.getBandLevelRange()[1] - eq.getBandLevelRange()[0]
                    progress = (eq.getBandLevel(i.toShort()) - eq.getBandLevelRange()[0]).toInt()
                    progressTintList = resources.getColorStateList(R.color.lavender_primary, null)
                    thumbTintList = resources.getColorStateList(R.color.lavender_primary, null)
                    
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                val bandLevel = (progress + eq.getBandLevelRange()[0]).toShort()
                                eq.setBandLevel(i.toShort(), bandLevel)
                                
                                val gainValue = bandLevel / 100f
                                gainLabel.text = String.format("%.1fdB", gainValue)
                            }
                        }
                        
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                }
                
                bandLayout.addView(gainLabel)
                bandLayout.addView(seekBar)
                bandLayout.addView(freqLabel)
                
                equalizerContainer.addView(bandLayout)
            }
        }
    }
    
    private fun setupPresets() {
        equalizer?.let { eq ->
            val presets = mutableListOf<String>()
            presets.add("Custom")
            
            for (i in 0 until eq.numberOfPresets) {
                presets.add(eq.getPresetName(i.toShort()))
            }
            
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                presets
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            
            presetSpinner.adapter = adapter
            presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) {
                        eq.usePreset((position - 1).toShort())
                        updateEqualizerBands()
                    }
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
    
    private fun updateEqualizerBands() {
        equalizer?.let { eq ->
            for (i in 0 until equalizerContainer.childCount) {
                val bandLayout = equalizerContainer.getChildAt(i) as LinearLayout
                val seekBar = bandLayout.getChildAt(1) as SeekBar
                val gainLabel = bandLayout.getChildAt(0) as TextView
                
                val bandLevel = eq.getBandLevel(i.toShort())
                seekBar.progress = (bandLevel - eq.getBandLevelRange()[0]).toInt()
                
                val gainValue = bandLevel / 100f
                gainLabel.text = String.format("%.1fdB", gainValue)
            }
        }
    }
    
    private fun setupControls() {
        // Equalizer switch
        equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->
            equalizer?.enabled = isChecked
            enableEqualizerBands(isChecked)
            presetSpinner.isEnabled = isChecked
        }
        
        // Bass boost switch
        bassBoostSwitch.setOnCheckedChangeListener { _, isChecked ->
            bassBoost?.enabled = isChecked
            bassBoostSeekBar.isEnabled = isChecked
        }
        
        // Bass boost seekbar
        bassBoostSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    bassBoost?.setStrength(progress.toShort())
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Virtualization switch
        virtualizationSwitch.setOnCheckedChangeListener { _, isChecked ->
            virtualizer?.enabled = isChecked
            virtualizationSeekBar.isEnabled = isChecked
        }
        
        // Virtualization seekbar
        virtualizationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    virtualizer?.setStrength(progress.toShort())
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun enableEqualizerBands(enabled: Boolean) {
        for (i in 0 until equalizerContainer.childCount) {
            val bandLayout = equalizerContainer.getChildAt(i) as LinearLayout
            val seekBar = bandLayout.getChildAt(1) as SeekBar
            seekBar.isEnabled = enabled
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
    }
}
