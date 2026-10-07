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
import com.example.services.ads.UnityAdsConfig
import com.example.services.ads.UnityAdsManager
import com.example.services.ads.findActivity

/**
 * Diagnostic card showing Unity Ads status and controls.
 * Strictly DEBUG-only: renders nothing in RELEASE builds.
 */
@Composable
fun UnityAdsDiagnosticsCard(modifier: Modifier = Modifier) {
    if (!BuildConfig.DEBUG) return

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("unity_ads_diagnostic_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Unity Ads Diagnostics (Debug Only)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Unity Ads Initialized:", fontSize = 13.sp)
                Text(
                    text = if (UnityAdsManager.isInitialized) "YES" else "NO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (UnityAdsManager.isInitialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Game ID:", fontSize = 13.sp)
                Text(
                    text = UnityAdsConfig.ANDROID_GAME_ID,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Test Mode:", fontSize = 13.sp)
                Text(
                    text = if (UnityAdsConfig.TEST_MODE) "ON (Debug)" else "OFF (Release)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Banner:", fontSize = 13.sp)
                Text(
                    text = UnityAdsManager.bannerStatus,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Interstitial:", fontSize = 13.sp)
                Text(
                    text = UnityAdsManager.interstitialStatus,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Rewarded:", fontSize = 13.sp)
                Text(
                    text = UnityAdsManager.rewardedStatus,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            if (UnityAdsManager.lastError != "None") {
                Column {
                    Text("Last Error:", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    Text(
                        text = UnityAdsManager.lastError,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        UnityAdsManager.preloadInterstitial(context)
                        UnityAdsManager.preloadRewarded(context)
                        Toast.makeText(context, "Reloading Ads...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reload Ads", fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        UnityAdsManager.showInterstitialIfAllowed(
                            activity = context.findActivity(),
                            ignoreCooldownForTest = true
                        ) {
                            Toast.makeText(context, "Interstitial ad completed/closed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Ad", fontSize = 12.sp)
                }
            }
        }
    }
}
