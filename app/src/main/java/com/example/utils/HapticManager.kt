package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central Haptic Manager for Digital Khata.
 * Provides subtle, lightweight haptic feedback for page transitions, back presses,
 * and key interactions.
 *
 * Persists the user's Vibration ON/OFF preference across sessions via SharedPreferences.
 * Gracefully handles devices without vibration hardware or permission discrepancies.
 */
object HapticManager {

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
        if (enabled) {
            performLightHaptic(context)
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
        performLightHaptic(context)
    }

    /**
     * Triggered on important validation errors.
     */
    fun error(context: Context) {
        if (!isVibrationEnabled(context)) return
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 20), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    /**
     * Executes standard light, subtle mobile UI haptic feedback.
     * Guaranteed never to throw or crash if hardware is unavailable.
     */
    fun performLightHaptic(context: Context) {
        if (!isVibrationEnabled(context)) return
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (_: Exception) {
            // Silently ignore if device doesn't support or permission issue
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }
}
