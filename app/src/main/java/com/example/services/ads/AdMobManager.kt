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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Production-ready Centralized Google AdMob Manager for Digital Khata.
 * Strictly uses official Google test IDs for App Open, Banner, Interstitial, and Rewarded ads.
 *
 * AdMob Test Unit IDs:
 * - App Open: ca-app-pub-3940256099942544/9257395921
 * - Banner: ca-app-pub-3940256099942544/6300978111
 * - Interstitial: ca-app-pub-3940256099942544/1033173712
 * - Rewarded: ca-app-pub-3940256099942544/5224354917
 */
object AdMobManager {

    private const val TAG = "ADMOB"

    var isSdkInitialized: Boolean by mutableStateOf(false)
        private set

    var bannerStatus: String by mutableStateOf("NOT READY")
    var interstitialStatus: String by mutableStateOf("NOT READY")
    var rewardedStatus: String by mutableStateOf("NOT READY")
    var appOpenStatus: String by mutableStateOf("NOT READY")
    var lastError: String by mutableStateOf("None")

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTime: Long = 0L

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenLoadTime: Long = 0L

    private const val INTERSTITIAL_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes
    private const val STARTUP_GRACE_PERIOD_MS = 60 * 1000L // 60 seconds
    private const val MAX_INTERSTITIALS_PER_24H = 3
    private const val PREFS_ADMOB = "digital_khata_admob_prefs"
    private const val KEY_SHOWN_TIMESTAMPS = "admob_interstitial_timestamps"

    private var appStartupTime = System.currentTimeMillis()

    val isInterstitialReady: Boolean
        get() = interstitialAd != null

    val isRewardedReady: Boolean
        get() = rewardedAd != null

    val isAppOpenReady: Boolean
        get() = appOpenAd != null && (System.currentTimeMillis() - appOpenLoadTime < 4 * 3600 * 1000L)

    /**
     * Initializes Google Mobile Ads SDK safely in the background on app start.
     * Never blocks UI, Firebase authentication, or main thread.
     */
    fun initialize(context: Context) {
        if (isSdkInitialized) {
            Log.d(TAG, "AdMob already initialized")
            return
        }
        appStartupTime = System.currentTimeMillis()
        Log.d(TAG, "AdMob initialization started")

        try {
            MobileAds.initialize(context.applicationContext) { status ->
                isSdkInitialized = true
                Log.d(TAG, "AdMob initialization complete: $status")
                preloadInterstitial(context.applicationContext)
                preloadRewarded(context.applicationContext)
                preloadAppOpenAd(context.applicationContext)
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Init Exception"
            lastError = "Init failed: $errorMsg"
            Log.e(TAG, "AdMob initialization failed: $errorMsg", e)
        }
    }

    /**
     * Preloads App Open Ad in background.
     */
    fun preloadAppOpenAd(context: Context) {
        if (isAppOpenLoading || isAppOpenReady) return
        isAppOpenLoading = true
        appOpenStatus = "LOADING"
        Log.d(TAG, "Loading App Open ad: ${AdMobConfig.APP_OPEN_AD_UNIT_ID}")

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context.applicationContext,
            AdMobConfig.APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    isAppOpenLoading = false
                    appOpenAd = ad
                    appOpenLoadTime = System.currentTimeMillis()
                    appOpenStatus = "READY"
                    Log.d(TAG, "App Open ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAppOpenLoading = false
                    appOpenAd = null
                    val errorDetails = loadAdError.message
                    appOpenStatus = "ERROR"
                    lastError = "App Open: $errorDetails"
                    Log.w(TAG, "App Open ad failed to load: $errorDetails")
                }
            }
        )
    }

    /**
     * Displays App Open Ad if available on launch/cold-start.
     */
    fun showAppOpenAdIfAvailable(activity: Activity?, onComplete: () -> Unit = {}) {
        if (activity == null || !isAppOpenReady) {
            Log.d(TAG, "App Open ad not available to show")
            onComplete()
            activity?.let { preloadAppOpenAd(it.applicationContext) }
            return
        }

        val ad = appOpenAd
        appOpenAd = null
        appOpenStatus = "NOT READY"

        ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open ad dismissed")
                activity.let { preloadAppOpenAd(it.applicationContext) }
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open ad failed to show: ${adError.message}")
                activity.let { preloadAppOpenAd(it.applicationContext) }
                onComplete()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open ad showing fullscreen")
            }
        }

        ad?.show(activity)
    }

    /**
     * Preloads Interstitial Ad in background.
     */
    fun preloadInterstitial(context: Context) {
        if (isInterstitialLoading || isInterstitialReady) return
        isInterstitialLoading = true
        interstitialStatus = "LOADING"
        Log.d(TAG, "Loading Interstitial ad: ${AdMobConfig.INTERSTITIAL_AD_UNIT_ID}")

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            AdMobConfig.INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isInterstitialLoading = false
                    interstitialAd = ad
                    interstitialStatus = "READY"
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isInterstitialLoading = false
                    interstitialAd = null
                    val errorDetails = loadAdError.message
                    interstitialStatus = "ERROR"
                    lastError = "Interstitial: $errorDetails"
                    Log.w(TAG, "Interstitial ad failed to load: $errorDetails")
                }
            }
        )
    }

    private fun canShowWithin24Hours(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_ADMOB, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SHOWN_TIMESTAMPS, "") ?: ""
        val now = System.currentTimeMillis()
        val timestamps = raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { now - it < 24 * 60 * 60 * 1000L }

        return timestamps.size < MAX_INTERSTITIALS_PER_24H
    }

    private fun recordInterstitialShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_ADMOB, Context.MODE_PRIVATE)
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
     * Shows Interstitial Ad with strict frequency capping:
     * - 60s startup grace period
     * - 5-minute cooldown between interstitials
     * - Max 3 per rolling 24 hours
     * - Preloads next ad upon dismissal
     * - Never blocks navigation or crashes if ad is unavailable.
     */
    fun showInterstitialIfAllowed(
        activity: Activity?,
        ignoreCooldownForTest: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        val now = System.currentTimeMillis()
        if (activity == null || !isInterstitialReady) {
            Log.d(TAG, "Interstitial show skipped: not ready")
            onComplete()
            activity?.let { preloadInterstitial(it.applicationContext) }
            return
        }

        if (!ignoreCooldownForTest) {
            if (now - appStartupTime < STARTUP_GRACE_PERIOD_MS) {
                Log.d(TAG, "Interstitial show skipped: in startup grace period")
                onComplete()
                return
            }

            if (now - lastInterstitialShownTime < INTERSTITIAL_COOLDOWN_MS) {
                Log.d(TAG, "Interstitial show skipped: cooldown active (5 mins)")
                onComplete()
                return
            }

            if (!canShowWithin24Hours(activity)) {
                Log.d(TAG, "Interstitial show skipped: 24-hour limit reached")
                onComplete()
                return
            }
        }

        val ad = interstitialAd
        interstitialAd = null
        interstitialStatus = "NOT READY"

        ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                lastInterstitialShownTime = System.currentTimeMillis()
                recordInterstitialShown(activity)
                activity.let { preloadInterstitial(it.applicationContext) }
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                activity.let { preloadInterstitial(it.applicationContext) }
                onComplete()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed fullscreen")
            }
        }

        try {
            ad?.show(activity)
        } catch (e: Exception) {
            Log.w(TAG, "Error displaying interstitial: ${e.message}")
            onComplete()
        }
    }

    /**
     * Preloads Rewarded Ad in background.
     */
    fun preloadRewarded(context: Context) {
        if (isRewardedLoading || isRewardedReady) return
        isRewardedLoading = true
        rewardedStatus = "LOADING"
        Log.d(TAG, "Loading Rewarded ad: ${AdMobConfig.REWARDED_AD_UNIT_ID}")

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            AdMobConfig.REWARDED_AD_UNIT_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    isRewardedLoading = false
                    rewardedAd = ad
                    rewardedStatus = "READY"
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isRewardedLoading = false
                    rewardedAd = null
                    val errorDetails = loadAdError.message
                    rewardedStatus = "ERROR"
                    lastError = "Rewarded: $errorDetails"
                    Log.w(TAG, "Rewarded ad failed to load: $errorDetails")
                }
            }
        )
    }

    /**
     * Shows Rewarded Ad when requested by user.
     */
    fun showRewarded(
        activity: Activity?,
        onRewardEarned: (String) -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        if (activity == null || !isRewardedReady) {
            Log.d(TAG, "Rewarded show skipped: not ready")
            onDismissed()
            activity?.let { preloadRewarded(it.applicationContext) }
            return
        }

        val ad = rewardedAd
        rewardedAd = null
        rewardedStatus = "NOT READY"

        ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                activity.let { preloadRewarded(it.applicationContext) }
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                activity.let { preloadRewarded(it.applicationContext) }
                onDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed fullscreen")
            }
        }

        try {
            ad?.show(activity) { rewardItem ->
                Log.d(TAG, "Reward earned: ${rewardItem.type} (${rewardItem.amount})")
                onRewardEarned(rewardItem.type)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error displaying rewarded ad: ${e.message}")
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
 * Responsive Google AdMob Banner Composable.
 * Features:
 * - Uses official Google AdMob Banner test ID ca-app-pub-3940256099942544/6300978111
 * - Standard fixed-size Banner (320x50 dp).
 * - Collapses automatically to 0dp if ad fails or while loading (no awkward empty white space).
 * - Safe lifecycle management: destroys AdView on release.
 * - Does not overlap bottom navigation or financial action bars.
 */
@Composable
fun AdMobBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.BANNER_AD_UNIT_ID
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
                .height(if (isLoaded) 50.dp else 0.dp)
                .testTag("admob_banner_ad"),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    val currentActivity = ctx.findActivity() ?: activity
                    val adView = AdView(currentActivity).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isLoaded = true
                                AdMobManager.bannerStatus = "READY"
                                Log.d("ADMOB_BANNER", "Ad loaded successfully: $adUnitId")
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                isLoaded = false
                                val err = loadAdError.message
                                AdMobManager.bannerStatus = "ERROR"
                                AdMobManager.lastError = "Banner: $err"
                                Log.w("ADMOB_BANNER", "Ad failed to load: $err")
                            }

                            override fun onAdClicked() {
                                Log.d("ADMOB_BANNER", "Ad clicked")
                            }

                            override fun onAdOpened() {
                                Log.d("ADMOB_BANNER", "Ad opened")
                            }

                            override fun onAdClosed() {
                                Log.d("ADMOB_BANNER", "Ad closed")
                            }
                        }
                    }
                    addView(adView)
                    adView.loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { container ->
                try {
                    for (i in 0 until container.childCount) {
                        val child = container.getChildAt(i)
                        if (child is AdView) {
                            child.destroy()
                        }
                    }
                    container.removeAllViews()
                } catch (e: Exception) {
                    Log.w("ADMOB_BANNER", "Error destroying banner: ${e.message}")
                }
            }
        )
    }
}
