package com.stroeer.ads.composedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stroeer.ads.StroeerSDK
import com.stroeer.ads.config.ConfigurationManager
import com.stroeer.compose.StroeerBanner
import com.stroeer.compose.StroeerInterstitial
import com.stroeer.compose.StroeerInterstitialController
import com.stroeer.compose.StroeerRewarded
import com.stroeer.compose.StroeerRewardedController

/** Hardcoded demo configuration. */
private const val APP_NAME = "appTest"

/** Banners shown in the scrolling feed. */
private val BANNER_SLOTS = listOf("b1", "b2", "b3", "b4", "b5", "b6")

/** Banner pinned to the bottom of the screen (does not scroll with the feed). */
private const val STICKY_FOOTER_SLOT = "stickyfooter"

/** Interstitial slot for the full-screen ad demo. */
private const val INTERSTITIAL_SLOT = "interstitial"

/** Rewarded slot for the rewarded ad demo. */
private const val REWARDED_SLOT = "rewarded"

/** Maps a banner slot id to a friendly UI label. */
private fun String.toDisplayName(): String = when (this) {
    STICKY_FOOTER_SLOT -> "stick footer"
    else -> this
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DemoApp(appContext = applicationContext)
                }
            }
        }
    }
}

private enum class ConfigState { LOADING, READY, FAILED }

@Composable
private fun DemoApp(appContext: android.content.Context) {
    // Banners can only load once remote config is ready, so gate the UI on it.
    var state by remember {
        mutableStateOf(
            if (ConfigurationManager.getInstance().isLoaded) ConfigState.READY
            else ConfigState.LOADING,
        )
    }

    LaunchedEffect(Unit) {
        if (state != ConfigState.READY) {
            StroeerSDK.setApplicationName(appContext, APP_NAME)
            // Suspends until config is fetched (or times out, returning null).
            ConfigurationManager.getInstance().getConfigAsync()
            state = if (ConfigurationManager.getInstance().isLoaded) {
                ConfigState.READY
            } else {
                ConfigState.FAILED
            }
        }
    }

    when (state) {
        ConfigState.READY -> BannerFeed()
        ConfigState.LOADING -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        ConfigState.FAILED -> Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Configuration failed to load for \"$APP_NAME\". " +
                    "Check the network connection and that the app name is valid.",
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * A scrolling feed of banners (b1..b6) interleaved with filler cards, with the
 * sticky-footer banner pinned to the bottom of the screen so it stays visible while
 * the feed scrolls. Off-screen feed banners stay unloaded until scrolled into view,
 * demonstrating the wrapper's lazy loading.
 */
@Composable
private fun BannerFeed() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Scrolling feed fills all space above the sticky footer.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "intro") {
                FillerCard(
                    title = "Stroeer Compose banner demo",
                    body = "Scroll down. Each banner below only requests an ad once it " +
                        "becomes visible on screen — the full-height filler sections in " +
                        "between keep the later banners off-screen until you reach them. " +
                        "The footer banner stays pinned to the bottom while you scroll.",
                )
            }

            item(key = "full-screen-controls") {
                FullScreenAdControls()
            }

            BANNER_SLOTS.forEachIndexed { index, slot ->
                item(key = "banner-$slot") {
                    BannerSlot(slotId = slot)
                }
                item(key = "filler-$slot") {
                    // Nearly a full screen tall so only one banner is near the viewport
                    // at a time — this makes the lazy loading visible while scrolling.
                    FillerCard(
                        title = "Filler section ${index + 1}",
                        body = FILLER_TEXT,
                        modifier = Modifier.fillParentMaxHeight(0.9f),
                    )
                }
            }
        }

        // Sticky footer banner: pinned below the feed, never scrolls.
        StickyFooterBanner()
    }
}

/**
 * Banner pinned to the bottom of the screen. Unlike the feed banners it is always
 * composed (and therefore loads immediately) and does not move while the feed scrolls.
 */
@Composable
private fun StickyFooterBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
    ) {
        BannerSlot(
            slotId = STICKY_FOOTER_SLOT,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
private fun BannerSlot(slotId: String, modifier: Modifier = Modifier) {
    // Reserve a standard banner height up front so the node has non-zero size and the
    // lazy-visibility check can fire; once loaded, snap to the real ad size.
    var bannerHeight by remember(slotId) { mutableStateOf(50.dp) }

    Card(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Banner: ${slotId.toDisplayName()}",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.labelMedium,
        )
        StroeerBanner(
            adSlotId = slotId,
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight),
            contentUrl = "https://www.stroeer.com",
            customTargeting = mapOf("placementName" to "composeDemo"),
            onLoaded = { banner ->
                // adSize is the resolved ad size in dp (e.g. 320x50).
                val h = banner.adSize.height
                if (h > 0) {
                    bannerHeight = h.dp
                }
            },
        )
    }
}

private enum class AdState { IDLE, LOADING, READY }

/**
 * Demo controls for interstitial and rewarded ads.
 * IDLE → tap "Load" → LOADING → onLoaded → READY → tap "Show" → IDLE.
 * If loading fails, state returns to IDLE so the user can retry with "Load".
 */
@Composable
private fun FullScreenAdControls() {
    var interstitialState by remember { mutableStateOf(AdState.IDLE) }
    var interstitialStatus by remember { mutableStateOf("") }
    var interstitialController: StroeerInterstitialController? by remember { mutableStateOf(null) }

    var rewardedState by remember { mutableStateOf(AdState.IDLE) }
    var rewardedStatus by remember { mutableStateOf("") }
    var rewardedEarnedStatus by remember { mutableStateOf("") }
    var rewardedController: StroeerRewardedController? by remember { mutableStateOf(null) }

    // Only mount the composables once the user taps Load; key() recreates them on retry.
    var interstitialKey by remember { mutableStateOf(0) }
    var rewardedKey by remember { mutableStateOf(0) }

    if (interstitialState != AdState.IDLE) {
        androidx.compose.runtime.key(interstitialKey) {
            interstitialController = StroeerInterstitial(
                adSlotId = INTERSTITIAL_SLOT,
                onLoaded = { interstitialState = AdState.READY; interstitialStatus = "Ready" },
                onFailed = { interstitialState = AdState.IDLE; interstitialStatus = "Load failed" },
                onDismissed = { interstitialState = AdState.IDLE; interstitialStatus = "Dismissed" },
                onFailedToShow = { interstitialState = AdState.IDLE; interstitialStatus = "Failed to show" },
            )
        }
    }

    if (rewardedState != AdState.IDLE) {
        androidx.compose.runtime.key(rewardedKey) {
            rewardedController = StroeerRewarded(
                adSlotId = REWARDED_SLOT,
                onLoaded = { rewardedState = AdState.READY; rewardedStatus = "Ready" },
                onFailed = { rewardedState = AdState.IDLE; rewardedStatus = "Load failed" },
                onUserEarnedReward = { item -> rewardedEarnedStatus = "Reward: ${item?.type} ×${item?.amount}" },
                onDismissed = { rewardedState = AdState.IDLE; rewardedStatus = "Dismissed" },
                onFailedToShow = { rewardedState = AdState.IDLE; rewardedStatus = "Failed to show" },
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Full-screen ads", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = interstitialState != AdState.LOADING,
                    onClick = {
                        when (interstitialState) {
                            AdState.IDLE -> { interstitialKey++; interstitialState = AdState.LOADING }
                            AdState.READY -> { interstitialState = AdState.IDLE; interstitialController?.show() }
                            AdState.LOADING -> { /* unreachable – button disabled */ }
                        }
                    },
                ) {
                    Text(when (interstitialState) {
                        AdState.IDLE -> "Load"
                        AdState.LOADING -> "Loading"
                        AdState.READY -> "Show"
                    })
                }
                Button(
                    enabled = rewardedState != AdState.LOADING,
                    onClick = {
                        when (rewardedState) {
                            AdState.IDLE -> { rewardedKey++; rewardedState = AdState.LOADING }
                            AdState.READY -> { rewardedState = AdState.IDLE; rewardedController?.show() }
                            AdState.LOADING -> { /* unreachable – button disabled */ }
                        }
                    },
                ) {
                    Text(when (rewardedState) {
                        AdState.IDLE -> "Load"
                        AdState.LOADING -> "Loading"
                        AdState.READY -> "Show"
                    })
                }
            }
            if (interstitialStatus.isNotEmpty()) {
                Text("Interstitial: $interstitialStatus", style = MaterialTheme.typography.bodySmall)
            }
            if (rewardedStatus.isNotEmpty()) {
                Text("Rewarded: $rewardedStatus", style = MaterialTheme.typography.bodySmall)
            }
            if (rewardedEarnedStatus.isNotEmpty()) {
                Text(rewardedEarnedStatus, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FillerCard(title: String, body: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "$title\n\n$body")
        }
    }
}

private const val FILLER_TEXT =
    "This is placeholder article content used to push the next banner below the fold. " +
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
    "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud " +
    "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute " +
    "irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
    "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia " +
    "deserunt mollit anim id est laborum."
