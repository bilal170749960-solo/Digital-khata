package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Robust Central Haptic & Vibration Manager for Digital Khata.
 * Provides noticeable, reliable tactile feedback for page transitions, back presses,
 * and key user actions.
 *
 * Persists the user's Vibration ON/OFF preference across sessions via SharedPreferences.
 * Gracefully handles all Android versions (including API 31+ VibratorManager, Android O VibrationEffect,
 * and legacy vibrator fallbacks) and devices without vibration hardware.
 */
object HapticManager {

    private const val TAG = "HAPTIC"
    private const val PREFS_NAME = "digital_khata_haptics"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private var isInitialized = false

    /**
     * Initializes the haptic manager and loads the saved vibration preference.
     */
    fun init(context: Context) {
        if (!isInitialized) {
            val prefs = getPrefs(context)
            val enabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
            _vibrationEnabled.value = enabled
            isInitialized = true
            Log.d(TAG, "HapticManager initialized: vibrationEnabled=$enabled")
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if vibration is enabled.
     */
    fun isVibrationEnabled(context: Context): Boolean {
        init(context)
        return _vibrationEnabled.value
    }

    /**
     * Updates and persists the user's vibration preference.
     */
    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        init(context)
        _vibrationEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
        Log.d(TAG, "Vibration preference set to: $enabled")
        if (enabled) {
            testVibration(context)
        }
    }

    /**
     * Triggered on every valid page/tab transition.
     */
    fun pageChange(context: Context) {
        performLightHaptic(context)
    }

    /**
     * Triggered on every Android system or in-app Back press.
     */
    fun backPress(context: Context) {
        performLightHaptic(context)
    }

    /**
     * Triggered on meaningful positive actions (QR scanned, payment confirmed, etc.)
     */
    fun importantAction(context: Context) {
        performNoticeableHaptic(context)
    }

    /**
     * Triggered on important validation errors.
     */
    fun error(context: Context) {
        if (!isVibrationEnabled(context)) return
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 50, 70, 50), -1)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()
                vibrator.vibrate(effect, audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 50, 70, 50), -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error performing error haptic: ${e.message}")
        }
    }

    /**
     * Executes standard tactile mobile UI haptic feedback (45ms).
     * Works reliably on all modern motors (ERM and LRA actuators).
     */
    fun performLightHaptic(context: Context) {
        if (!isVibrationEnabled(context)) return
        vibrate(context, durationMs = 45L)
    }

    /**
     * Executes slightly stronger haptic feedback for key operations (80ms).
     */
    fun performNoticeableHaptic(context: Context) {
        if (!isVibrationEnabled(context)) return
        vibrate(context, durationMs = 80L)
    }

    /**
     * Test vibration used when user switches vibration ON or clicks Test Vibration in settings (100ms).
     */
    fun testVibration(context: Context) {
        vibrate(context, durationMs = 100L)
    }

    /**
     * Core universal vibration executor.
     * Uses AudioAttributes to bypass notification silence filters for tactile feedback.
     */
    private fun vibrate(context: Context, durationMs: Long) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) {
                Log.d(TAG, "Device reports no physical vibrator motor")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()
                vibrator.vibrate(effect, audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
            Log.d(TAG, "Vibrated successfully for ${durationMs}ms")
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed: ${e.message}")
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire Vibrator service: ${e.message}")
            null
        }
    }
}
