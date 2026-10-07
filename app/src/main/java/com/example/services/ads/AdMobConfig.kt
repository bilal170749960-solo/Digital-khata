package com.example.services.ads

/**
 * Centralized configuration for Google AdMob in Digital Khata.
 * Strictly uses official Google AdMob test ad unit IDs as requested:
 * - App Open: ca-app-pub-3940256099942544/9257395921
 * - Fixed Sized Banner: ca-app-pub-3940256099942544/6300978111
 * - Interstitial: ca-app-pub-3940256099942544/1033173712
 * - Rewarded: ca-app-pub-3940256099942544/5224354917
 */
object AdMobConfig {
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Official sample test application ID for Android
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
}
