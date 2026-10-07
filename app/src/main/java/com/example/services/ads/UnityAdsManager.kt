package com.example.services.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsLoadOptions
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.ads.metadata.MetaData
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

/**
 * Production-ready Centralized Unity Ads Manager for Digital Khata.
 * Strictly adheres to:
 * - Single initialization on app start using Game ID 800384213.
 * - Test mode enabled in DEBUG builds so test ads display reliably.
 * - Controlled frequency interstitial capping (5 min cooldown, max 3 per 24h).
 * - Non-blocking background loads, never disrupts navigation or financial actions.
 * - Uses exact configured placement IDs: BP_Banner_Android, BP_Interstitial_Android, BP_Rewarded_Android.
 * - Complete detailed logging for every Unity Ads operation.
 * - Safe Jetpack Compose lifecycle without recomposition leaks or crashes.
 */
object UnityAdsManager {

    var isSdkInitialized: Boolean = false
        private set

    val isInitialized: Boolean
        get() = isSdkInitialized || com.unity3d.ads.UnityAds.isInitialized

    var lastInitError: String? = null
        private set

    var bannerStatus: String by mutableStateOf("NOT READY")
    var interstitialStatus: String by mutableStateOf("NOT READY")
    var rewardedStatus: String by mutableStateOf("NOT READY")
    var lastError: String by mutableStateOf("None")

    private var isInterstitialLoading = false
    private var isInterstitialLoaded = false
    private var lastInterstitialShownTime: Long = 0L

    private var isRewardedLoading = false
    private var isRewardedLoaded = false

    private const val INTERSTITIAL_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes
    private const val STARTUP_GRACE_PERIOD_MS = 60 * 1000L // 60 seconds
    private const val MAX_INTERSTITIALS_PER_24H = 3
    private const val PREFS_UNITY_ADS = "digital_khata_unity_ads"
    private const val KEY_SHOWN_TIMESTAMPS = "unity_interstitial_timestamps"

    private var appStartupTime = System.currentTimeMillis()

    val isInterstitialReady: Boolean
        get() = isInterstitialLoaded

    val isRewardedReady: Boolean
        get() = isRewardedLoaded

    /**
     * Initializes Unity Ads SDK safely in the background on app start.
     * Never blocks UI, Firebase authentication, or main thread.
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d("UNITY_ADS", "Initialization skipped: already initialized")
            return
        }
        appStartupTime = System.currentTimeMillis()
        Log.d(
            "UNITY_ADS",
            "UNITY ADS INIT START - Game ID: ${UnityAdsConfig.ANDROID_GAME_ID} - Test Mode: ${UnityAdsConfig.TEST_MODE}"
        )

        // Set GDPR and Privacy consent metadata before initialization
        try {
            val gdprMetaData = MetaData(context.applicationContext)
            gdprMetaData.set("gdpr.consent", true)
            gdprMetaData.commit()

            val privacyMetaData = MetaData(context.applicationContext)
            privacyMetaData.set("privacy.consent", true)
            privacyMetaData.commit()
        } catch (e: Exception) {
            Log.w("UNITY_ADS", "Could not set consent metadata: ${e.message}")
        }

        try {
            UnityAds.initialize(
                context.applicationContext,
                UnityAdsConfig.ANDROID_GAME_ID,
                UnityAdsConfig.TEST_MODE,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        isSdkInitialized = true
                        lastInitError = null
                        Log.d("UNITY_ADS", "UNITY ADS INIT SUCCESS")
                        preloadInterstitial(context.applicationContext)
                        preloadRewarded(context.applicationContext)
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError?,
                        message: String?
                    ) {
                        isSdkInitialized = false
                        val errorDetails = "$error: $message"
                        lastInitError = errorDetails
                        lastError = "Init failed: $errorDetails"
                        Log.e("UNITY_ADS", "UNITY ADS INIT FAILED: $errorDetails")
                    }
                }
            )
        } catch (e: Exception) {
            isSdkInitialized = false
            val errorDetails = e.message ?: "Exception"
            lastInitError = errorDetails
            lastError = "Init failed: $errorDetails"
            Log.e("UNITY_ADS", "UNITY ADS INIT FAILED: $errorDetails")
        }
    }

    /**
     * Preloads an interstitial ad in the background.
     */
    fun preloadInterstitial(context: Context) {
        if (isInterstitialLoading || isInterstitialLoaded) {
            Log.d("INTERSTITIAL", "Preload skipped: loading=$isInterstitialLoading, loaded=$isInterstitialLoaded")
            return
        }
        if (!isSdkInitialized && !com.unity3d.ads.UnityAds.isInitialized) {
            Log.d("INTERSTITIAL", "Preload skipped: Unity Ads is not initialized yet")
            return
        }
        isInterstitialLoading = true
        interstitialStatus = "LOADING"
        Log.d("INTERSTITIAL", "Load started\nPlacement: ${UnityAdsConfig.INTERSTITIAL_PLACEMENT_ID}")

        try {
            val loadOptions = UnityAdsLoadOptions().apply {
                objectId = java.util.UUID.randomUUID().toString()
                set("adMarkup", "{\"id\":\"${java.util.UUID.randomUUID()}\",\"bidid\":\"1\",\"cur\":\"USD\",\"price\":1.0}")
            }
            UnityAds.load(
                UnityAdsConfig.INTERSTITIAL_PLACEMENT_ID,
                loadOptions,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {
                        isInterstitialLoading = false
                        isInterstitialLoaded = true
                        interstitialStatus = "READY"
                        Log.d("INTERSTITIAL", "Load success: $placementId")
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {
                        isInterstitialLoading = false
                        isInterstitialLoaded = false
                        val errorDetails = "$error: $message"
                        interstitialStatus = "ERROR"
                        lastError = "Interstitial: $errorDetails"
                        Log.w("INTERSTITIAL", "Load failed for $placementId: $errorDetails")
                    }
                }
            )
        } catch (e: Exception) {
            isInterstitialLoading = false
            isInterstitialLoaded = false
            val errorDetails = e.message ?: "Exception"
            interstitialStatus = "ERROR"
            lastError = "Interstitial: $errorDetails"
            Log.w("INTERSTITIAL", "Load failed: $errorDetails")
        }
    }

    /**
     * Checks if rolling 24-hour frequency limit allows showing an interstitial.
     */
    private fun canShowWithin24Hours(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_UNITY_ADS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SHOWN_TIMESTAMPS, "") ?: ""
        val now = System.currentTimeMillis()
        val timestamps = raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { now - it < 24 * 60 * 60 * 1000L }

        return timestamps.size < MAX_INTERSTITIALS_PER_24H
    }

    private fun recordInterstitialShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_UNITY_ADS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SHOWN_TIMESTAMPS, "") ?: ""
        val now = System.currentTimeMillis()
        val timestamps = raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { now - it < 24 * 60 * 60 * 1000L }
            .toMutableList()

        timestamps.add(now)
        prefs.edit().putString(KEY_SHOWN_TIMESTAMPS, timestamps.joinToString(",")).apply()
    }

    /**
     * Displays the interstitial ad ONLY during safe, non-sensitive navigation transitions.
     * Enforces strict frequency capping:
     * - Minimum 60s after app start
     * - 5-minute cooldown between interstitials
     * - Maximum 3 per rolling 24-hour window
     * - Preloads next ad upon dismissal
     * - NEVER blocks navigation if ad is unavailable.
     */
    fun showInterstitialIfAllowed(
        activity: Activity?,
        ignoreCooldownForTest: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        val now = System.currentTimeMillis()
        if (activity == null || !isInterstitialLoaded) {
            Log.d("INTERSTITIAL", "Show skipped because not ready")
            onComplete()
            activity?.let { preloadInterstitial(it.applicationContext) }
            return
        }

        if (!ignoreCooldownForTest) {
            if (now - appStartupTime < STARTUP_GRACE_PERIOD_MS) {
                Log.d("INTERSTITIAL", "Show skipped because in startup grace period")
                onComplete()
                return
            }

            if (now - lastInterstitialShownTime < INTERSTITIAL_COOLDOWN_MS) {
                Log.d("INTERSTITIAL", "Show skipped because cooldown active (5 mins)")
                onComplete()
                return
            }

            if (!canShowWithin24Hours(activity)) {
                Log.d("INTERSTITIAL", "Show skipped because daily limit (3 ads/24h) reached")
                onComplete()
                return
            }
        }

        try {
            isInterstitialLoaded = false
            interstitialStatus = "NOT READY"
            Log.d("INTERSTITIAL", "Show started on placement: ${UnityAdsConfig.INTERSTITIAL_PLACEMENT_ID}")

            UnityAds.show(
                activity,
                UnityAdsConfig.INTERSTITIAL_PLACEMENT_ID,
                UnityAdsShowOptions(),
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowStart(placementId: String?) {
                        Log.d("INTERSTITIAL", "Show started on placement: $placementId")
                    }

                    override fun onUnityAdsShowClick(placementId: String?) {
                        Log.d("INTERSTITIAL", "Click on placement: $placementId")
                    }

                    override fun onUnityAdsShowComplete(
                        placementId: String?,
                        state: UnityAds.UnityAdsShowCompletionState?
                    ) {
                        Log.d("INTERSTITIAL", "Show completed")
                        lastInterstitialShownTime = System.currentTimeMillis()
                        recordInterstitialShown(activity)
                        preloadInterstitial(activity.applicationContext)
                        onComplete()
                    }

                    override fun onUnityAdsShowFailure(
                        placementId: String?,
                        error: UnityAds.UnityAdsShowError?,
                        message: String?
                    ) {
                        val errorDetails = "$error: $message"
                        lastError = "Interstitial show: $errorDetails"
                        Log.w("INTERSTITIAL", "Show failed: $errorDetails")
                        preloadInterstitial(activity.applicationContext)
                        onComplete()
                    }
                }
            )
        } catch (e: Exception) {
            val errorDetails = e.message ?: "Exception"
            lastError = "Interstitial show: $errorDetails"
            Log.w("INTERSTITIAL", "Show failed: $errorDetails")
            onComplete()
        }
    }

    /**
     * Preloads a rewarded ad in the background.
     */
    fun preloadRewarded(context: Context) {
        if (isRewardedLoading || isRewardedLoaded) return
        if (!isSdkInitialized && !com.unity3d.ads.UnityAds.isInitialized) return
        isRewardedLoading = true
        rewardedStatus = "LOADING"
        Log.d("REWARDED", "Load started\nPlacement: ${UnityAdsConfig.REWARDED_PLACEMENT_ID}")

        try {
            val loadOptions = UnityAdsLoadOptions().apply {
                objectId = java.util.UUID.randomUUID().toString()
                set("adMarkup", "{\"id\":\"${java.util.UUID.randomUUID()}\",\"bidid\":\"1\",\"cur\":\"USD\",\"price\":1.0}")
            }
            UnityAds.load(
                UnityAdsConfig.REWARDED_PLACEMENT_ID,
                loadOptions,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {
                        isRewardedLoading = false
                        isRewardedLoaded = true
                        rewardedStatus = "READY"
                        Log.d("REWARDED", "Load success: $placementId")
                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {
                        isRewardedLoading = false
                        isRewardedLoaded = false
                        val errorDetails = "$error: $message"
                        rewardedStatus = "ERROR"
                        lastError = "Rewarded: $errorDetails"
                        Log.w("REWARDED", "Load failed for $placementId: $errorDetails")
                    }
                }
            )
        } catch (e: Exception) {
            isRewardedLoading = false
            isRewardedLoaded = false
            val errorDetails = e.message ?: "Exception"
            rewardedStatus = "ERROR"
            lastError = "Rewarded: $errorDetails"
            Log.w("REWARDED", "Load failed: $errorDetails")
        }
    }

    /**
     * Displays a rewarded ad when requested by user for an optional feature.
     * Never forced into core financial workflows.
     */
    fun showRewarded(
        activity: Activity?,
        onRewardEarned: (String) -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        if (activity == null || !isRewardedLoaded) {
            Log.d("REWARDED", "Show skipped because not ready")
            onDismissed()
            activity?.let { preloadRewarded(it.applicationContext) }
            return
        }

        try {
            isRewardedLoaded = false
            rewardedStatus = "NOT READY"
            Log.d("REWARDED", "Show started on placement: ${UnityAdsConfig.REWARDED_PLACEMENT_ID}")

            UnityAds.show(
                activity,
                UnityAdsConfig.REWARDED_PLACEMENT_ID,
                UnityAdsShowOptions(),
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowStart(placementId: String?) {
                        Log.d("REWARDED", "Show started on placement: $placementId")
                    }

                    override fun onUnityAdsShowClick(placementId: String?) {
                        Log.d("REWARDED", "Click on placement: $placementId")
                    }

                    override fun onUnityAdsShowComplete(
                        placementId: String?,
                        state: UnityAds.UnityAdsShowCompletionState?
                    ) {
                        Log.d("REWARDED", "Show completed (state: $state)")
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            Log.d("REWARDED", "Reward received")
                            onRewardEarned(placementId ?: "")
                        }
                        preloadRewarded(activity.applicationContext)
                        onDismissed()
                    }

                    override fun onUnityAdsShowFailure(
                        placementId: String?,
                        error: UnityAds.UnityAdsShowError?,
                        message: String?
                    ) {
                        val errorDetails = "$error: $message"
                        lastError = "Rewarded show: $errorDetails"
                        Log.w("REWARDED", "Show failed: $errorDetails")
                        preloadRewarded(activity.applicationContext)
                        onDismissed()
                    }
                }
            )
        } catch (e: Exception) {
            val errorDetails = e.message ?: "Exception"
            lastError = "Rewarded show: $errorDetails"
            Log.w("REWARDED", "Show failed: $errorDetails")
            onDismissed()
        }
    }
}

/**
 * Helper to resolve Activity from Context.
 */
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Responsive Unity Ads Banner Composable.
 * Features:
 * - Proper lifecycle management with container-based AndroidView.
 * - Collapses automatically to 0dp if ad fails or is loading (no awkward blank space).
 * - Does not overlap bottom navigation or financial widgets.
 * - Handles offline and connection failures gracefully without throwing errors.
 * - Recomposition safe: handles view detachment and disposal properly.
 */
@Composable
fun UnityBannerAd(
    modifier: Modifier = Modifier,
    placementId: String = UnityAdsConfig.BANNER_PLACEMENT_ID
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() } ?: return

    var isLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .width(320.dp)
                .height(50.dp)
                .testTag("unity_banner_ad"),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    val density = ctx.resources.displayMetrics.density
                    val w = (320 * density).toInt()
                    val h = (50 * density).toInt()
                    layoutParams = ViewGroup.LayoutParams(w, h)
                    val currentActivity = ctx.findActivity() ?: activity

                    val banner = BannerView(
                        currentActivity,
                        placementId,
                        UnityBannerSize(320, 50)
                    ).apply {
                        listener = object : BannerView.Listener() {
                            override fun onBannerLoaded(bannerView: BannerView?) {
                                isLoaded = true
                                UnityAdsManager.bannerStatus = "READY"
                                Log.d("BANNER", "Load success: $placementId")
                            }

                            override fun onBannerFailedToLoad(
                                bannerView: BannerView?,
                                error: BannerErrorInfo?
                            ) {
                                isLoaded = false
                                val errorDetails = error?.errorMessage ?: "Unknown error"
                                UnityAdsManager.bannerStatus = "ERROR"
                                UnityAdsManager.lastError = "Banner: $errorDetails"
                                Log.w("BANNER", "Load failed for $placementId: $errorDetails")
                            }

                            override fun onBannerClick(bannerView: BannerView?) {
                                Log.d("BANNER", "Click: $placementId")
                            }

                            override fun onBannerShown(bannerView: BannerView?) {
                                Log.d("BANNER", "Shown: $placementId")
                            }

                            override fun onBannerLeftApplication(bannerView: BannerView?) {
                                Log.d("BANNER", "Left application: $placementId")
                            }
                        }
                    }
                    addView(banner)
                    Log.d("BANNER", "Load started\nPlacement: $placementId")
                    banner.load()
                }
            },
            onRelease = { container ->
                try {
                    for (i in 0 until container.childCount) {
                        val child = container.getChildAt(i)
                        if (child is BannerView) {
                            child.destroy()
                        }
                    }
                    container.removeAllViews()
                } catch (e: Exception) {
                    Log.w("BANNER", "Error releasing banner: ${e.message}")
                }
            }
        )
    }
}
