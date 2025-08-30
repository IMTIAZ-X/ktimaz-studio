package com.ktimazstudio.managers

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ktimazstudio.R

// --- SoundEffectManager ---
/**
 * Manages playing short sound effects using SoundPool for low-latency audio feedback.
 * This is used for click sounds and other UI interactions.
 *
 * IMPORTANT: For this to work, you must place an audio file (e.g., click_sound.wav)
 * in your `res/raw` directory.
 */
class SoundEffectManager(private val context: Context, private val sharedPrefsManager: SharedPreferencesManager) {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0

    /**
     * Loads the sound effects into the SoundPool.
     * This should be called once, typically during app startup.
     */
    fun loadSounds() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME) // Appropriate usage for UI sounds
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1) // Only one click sound stream at a time to prevent overlap
            .setAudioAttributes(audioAttributes)
            .build()

        // Load your click sound from res/raw.
        // Make sure you have a file named 'click_sound.wav' (or .mp3) in res/raw.
        // You will need to create the 'raw' directory inside 'app/src/main/res/'
        // and place your sound file there.
        clickSoundId = soundPool?.load(context, R.raw.click_sound, 1) ?: 0
    }

    /**
     * Plays the loaded click sound, only if sound effects are enabled in settings.
     */
    fun playClickSound() {
        if (sharedPrefsManager.isSoundEnabled() && clickSoundId != 0) {
            soundPool?.play(clickSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    /**
     * Releases the SoundPool resources.
     * This should be called when the activity/application is destroyed to prevent memory leaks.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}