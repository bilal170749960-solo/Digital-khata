package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.services.ads.AdMobConfig
import com.example.services.ads.AdMobManager
import com.example.services.ads.findActivity

/**
 * Diagnostic card showing Google AdMob official test IDs, real-time load statuses, and controls.
 * Visible in DEBUG mode for verification.
 */
@Composable
fun AdMobDiagnosticsCard(modifier: Modifier = Modifier) {
    if (!BuildConfig.DEBUG) return

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_diagnostic_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Google AdMob Configuration & Test IDs",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AdMob Initialized:", fontSize = 13.sp)
                Text(
                    text = if (AdMobManager.isSdkInitialized) "YES" else "INITIALIZING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (AdMobManager.isSdkInitialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Official Google Test Unit IDs:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                AdUnitRow(label = "App Open", id = AdMobConfig.APP_OPEN_AD_UNIT_ID, status = AdMobManager.appOpenStatus)
                AdUnitRow(label = "Fixed Banner", id = AdMobConfig.BANNER_AD_UNIT_ID, status = AdMobManager.bannerStatus)
                AdUnitRow(label = "Interstitial", id = AdMobConfig.INTERSTITIAL_AD_UNIT_ID, status = AdMobManager.interstitialStatus)
                AdUnitRow(label = "Rewarded", id = AdMobConfig.REWARDED_AD_UNIT_ID, status = AdMobManager.rewardedStatus)
            }

            if (AdMobManager.lastError != "None") {
                Text(
                    text = "Last Status/Notice: ${AdMobManager.lastError}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            AdMobManager.showInterstitialIfAllowed(
                                activity = activity,
                                ignoreCooldownForTest = true
                            ) {
                                Toast.makeText(context, "Interstitial ad dismissed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_test_interstitial"),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text("Test Interstitial", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            AdMobManager.showRewarded(
                                activity = activity,
                                onRewardEarned = { type ->
                                    Toast.makeText(context, "Reward earned: $type", Toast.LENGTH_LONG).show()
                                },
                                onDismissed = {
                                    Toast.makeText(context, "Rewarded ad closed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_test_rewarded"),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text("Test Rewarded", fontSize = 11.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            AdMobManager.showAppOpenAdIfAvailable(activity) {
                                Toast.makeText(context, "App Open ad closed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_test_app_open"),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text("Test App Open", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        AdMobManager.preloadInterstitial(context)
                        AdMobManager.preloadRewarded(context)
                        AdMobManager.preloadAppOpenAd(context)
                        Toast.makeText(context, "Reloading AdMob test ads...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_reload_admob"),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text("Reload Ads", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AdUnitRow(label: String, id: String, status: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$label:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(
                status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = when (status) {
                    "READY" -> MaterialTheme.colorScheme.primary
                    "LOADING" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Text(id, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
