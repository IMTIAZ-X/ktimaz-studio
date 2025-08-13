package com.ktimazstudio.old.managers

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import com.ktimazstudio.managers.SharedPreferencesManager

/**
 * Classic Sound Manager - Simple tone generation
 */
class OldSoundEffectManager(
    private val context: Context,
    private val sharedPrefsManager: SharedPreferencesManager
) {
    private var toneGenerator: ToneGenerator? = null
    
    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
        } catch (e: RuntimeException) {
            // ToneGenerator creation failed
        }
    }
    
    fun playClickSound() {
        if (sharedPrefsManager.isSoundEnabled()) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        }
    }
    
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}