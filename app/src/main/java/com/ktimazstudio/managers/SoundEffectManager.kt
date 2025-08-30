package com.ktimazstudio.managers

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import com.ktimazstudio.R

/**
 * Enhanced SoundEffectManager with fallback system
 * Falls back to system tones if custom sounds are not available
 */
class SoundEffectManager(
    private val context: Context, 
    private val sharedPrefsManager: SharedPreferencesManager
) {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var toneGenerator: ToneGenerator? = null
    private var useSystemSounds = false

    /**
     * Loads the sound effects with fallback to system sounds
     */
    fun loadSounds() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()

            // Try to load custom sound first
            try {
                clickSoundId = soundPool?.load(context, R.raw.click_sound, 1) ?: 0
                if (clickSoundId == 0) {
                    throw Exception("Failed to load custom sound")
                }
            } catch (e: Exception) {
                // Fallback to system tone generator
                useSystemSounds = true
                soundPool?.release()
                soundPool = null
                
                try {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
                } catch (toneException: RuntimeException) {
                    // If tone generator also fails, disable sounds completely
                    toneGenerator = null
                }
            }
        } catch (e: Exception) {
            // Complete fallback - disable sounds
            useSystemSounds = true
            toneGenerator = null
        }
    }

    /**
     * Plays the click sound using available method
     */
    fun playClickSound() {
        if (!sharedPrefsManager.isSoundEnabled()) return
        
        try {
            when {
                !useSystemSounds && clickSoundId != 0 -> {
                    // Use custom sound
                    soundPool?.play(clickSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
                }
                useSystemSounds && toneGenerator != null -> {
                    // Use system tone
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                }
                else -> {
                    // No sound available - silent operation
                }
            }
        } catch (e: Exception) {
            // Ignore sound errors to prevent crashes
        }
    }

    /**
     * Releases all sound resources
     */
    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}