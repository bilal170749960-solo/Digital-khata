package com.example.services.ads

import com.example.BuildConfig

/**
 * Centralized configuration for Unity Ads in Digital Khata.
 * Strictly holds official game and placement IDs as specified.
 */
object UnityAdsConfig {
    const val ANDROID_GAME_ID = "800384213"

    // Official Placement IDs as configured in the Unity Monetization Dashboard
    const val BANNER_PLACEMENT_ID = "BP_Banner_Android"
    const val INTERSTITIAL_PLACEMENT_ID = "BP_Interstitial_Android"
    const val REWARDED_PLACEMENT_ID = "BP_Rewarded_Android"

    // Use test mode in DEBUG builds so test creatives reliably load and display.
    // In RELEASE builds, test mode is disabled for real production ad serving.
    val TEST_MODE: Boolean
        get() = BuildConfig.DEBUG
}
