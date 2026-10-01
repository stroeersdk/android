# Jetpack Compose Integration Manual

## Contents

- [1. Overview](#1-overview)
- [2. Before You Start](#2-before-you-start)
- [3. Try the Example App](#3-try-the-example-app)
- [4. Add SDK Dependencies](#4-add-sdk-dependencies)
    - [4.1. Configure Repositories](#41-configure-repositories)
    - [4.2. Add Dependencies](#42-add-dependencies)
    - [4.3. Enable Compose](#43-enable-compose)
- [5. Initialize the SDK](#5-initialize-the-sdk)
- [6. Banner Ads](#6-banner-ads)
    - [6.1. StroeerBanner](#61-stroeerbanner)
    - [6.2. Reserve Space for the Banner](#62-reserve-space-for-the-banner)
    - [6.3. Lazy Loading and Visibility](#63-lazy-loading-and-visibility)
    - [6.4. Adjust the Height After Loading](#64-adjust-the-height-after-loading)
- [7. Interstitial Ads](#7-interstitial-ads)
    - [7.1. StroeerInterstitial](#71-stroeerinterstitial)
    - [7.2. Load and Show Pattern](#72-load-and-show-pattern)
- [8. Rewarded Ads](#8-rewarded-ads)
    - [8.1. StroeerRewarded](#81-stroeerrewarded)
    - [8.2. Granting the Reward](#82-granting-the-reward)
- [9. Lifecycle](#9-lifecycle)
- [10. Error Handling](#10-error-handling)
- [11. Differences from the View API](#11-differences-from-the-view-api)

> [!Note]
> This library provides a basic Jetpack Compose wrapper that uses the standard lifecycle of StroeerBannerView and Google’s AdView.  
> If your application requires more advanced lifecycle management, implement your own Compose wrapper using the native SDK.
## 1. Overview

The `compose` module provides Jetpack Compose wrappers for the StröerSDK ad formats:

| Composable | Emits a UI node | Returns |
| --- | --- | --- |
| `StroeerBanner` | Yes | `Unit` |
| `StroeerInterstitial` | No | `StroeerInterstitialController` |
| `StroeerRewarded` | No | `StroeerRewardedController` |

The wrappers manage the underlying ad view for you: they create it when the composable enters composition and destroy it when it leaves. Banners additionally load lazily, requesting an ad only once they are actually visible on screen.

The module is optional. The View-based API described in the [main integration manual](Integration.md) remains available, and both can be used in the same app.

## 2. Before You Start

Complete the main Android SDK integration before adding the Compose module. The Compose wrappers are a thin layer over the same ad pipeline and share its configuration, consent handling and targeting.

The SDK requires an **Activity** context. The composables resolve the Activity from `LocalContext` automatically, including through `ContextWrapper` chains such as `ContextThemeWrapper` or a `Dialog`.

> [!IMPORTANT]
> All ad formats require an Activity. Compose previews and tests that supply a non-Activity context will not load ads.

## 3. Try the Example App

Use the [Compose example app](https://github.com/stroeersdk/android/tree/main/example/compose) to explore banners in a scrolling feed, a sticky footer banner, and the interstitial and rewarded flows before integrating into your own application.

## 4. Add SDK Dependencies

### 4.1. Configure Repositories

Add the following repository to your existing Gradle repository configuration.

**Kotlin DSL (`settings.gradle.kts`)**

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://stroeersdk.github.io/android/maven")
            content {
                includeGroup("com.stroeer.ads")
            }
        }
    }
}
```

**Groovy (`settings.gradle`)**

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()

        maven {
            url 'https://stroeersdk.github.io/android/maven'
            content {
                includeGroup 'com.stroeer.ads'
            }
        }
    }
}
```

### 4.2. Add Dependencies

Add the following dependencies to your app-level Gradle file. Replace `<version>` with your StröerSDK version, using the same version for the `core` and `compose` modules.

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
dependencies {
    implementation("com.stroeer.ads:core:<version>")
    implementation("com.stroeer.ads:compose:<version>")
}
```

**Groovy (`build.gradle`)**

```groovy
dependencies {
    implementation 'com.stroeer.ads:core:<version>'
    implementation 'com.stroeer.ads:compose:<version>'
}
```

> [!IMPORTANT]
> Declare `core` explicitly even though `compose` depends on it. The dependency is published at `runtime` scope, so it is **not** on your compile classpath automatically. Types that appear in the Compose callbacks — `StroeerBannerView`, `StroeerException` — will not resolve without it.

The rewarded callbacks expose Google Mobile Ads types (`RewardedAd`, `RewardItem`). If you use them, the Google Mobile Ads SDK must also be on your compile classpath.

### 4.3. Enable Compose

The module ships `androidx.compose.ui:ui` and `androidx.compose.runtime:runtime` at runtime scope only, so declare your own Compose dependencies as usual. Enable the Compose build feature in your app module:

```kotlin
android {
    buildFeatures {
        compose = true
    }
}
```

> [!NOTE]
> The module is built against Compose `1.11.4` and Kotlin `2.1.0`, with `minSdk 24` and Java 17.

## 5. Initialize the SDK

Set the application name once when your app starts, exactly as in the View-based integration:

```kotlin
StroeerSDK.setApplicationName(applicationContext, "APPLICATION_NAME")
```

This starts an asynchronous configuration fetch. Ads requested before the configuration has arrived fail with a configuration error, so gate your ad UI on it:

```kotlin
var configReady by remember {
    mutableStateOf(ConfigurationManager.getInstance().isLoaded)
}

LaunchedEffect(Unit) {
    if (!configReady) {
        StroeerSDK.setApplicationName(appContext, APP_NAME)
        // Suspends until the configuration is fetched, or times out.
        ConfigurationManager.getInstance().getConfigAsync()
        configReady = ConfigurationManager.getInstance().isLoaded
    }
}

if (configReady) {
    AdContent()
}
```

## 6. Banner Ads

### 6.1. StroeerBanner

```kotlin
@Composable
fun StroeerBanner(
    adSlotId: String,
    modifier: Modifier = Modifier,
    contentUrl: String? = null,
    customTargeting: Map<String, String> = emptyMap(),
    visibilityThreshold: Float = 0.5f,
    onLoaded: (StroeerBannerView) -> Unit = {},
    onFailed: (StroeerBannerView, StroeerException) -> Unit = { _, _ -> },
    onOpened: (StroeerBannerView) -> Unit = {},
    onClosed: (StroeerBannerView) -> Unit = {},
    onClicked: (StroeerBannerView) -> Unit = {},
    onImpression: (StroeerBannerView) -> Unit = {},
)
```

Only `adSlotId` is required.

| Parameter | Description |
| --- | --- |
| `adSlotId` | The publisher ad slot name. Changing it destroys the current banner and creates a new one. |
| `modifier` | Applied to the hosting view. Must reserve non-zero space — see [6.2](#62-reserve-space-for-the-banner). |
| `contentUrl` | Optional content URL used for targeting. |
| `customTargeting` | Per-banner key/value targeting, one value per key. |
| `visibilityThreshold` | Fraction of the banner area (`0f`–`1f`) that must be on screen before the ad is requested. Defaults to `0.5f`. Values outside the range are clamped. |

| Callback | Fires when |
| --- | --- |
| `onLoaded` | The banner finished loading. |
| `onFailed` | The request failed, including no fill. |
| `onOpened` | The ad opened a full-screen overlay, typically after a click. |
| `onClosed` | That overlay closed and the user returned to your app. |
| `onClicked` | The ad was clicked. |
| `onImpression` | An impression was recorded. |

> [!NOTE]
> `contentUrl` and `customTargeting` are applied immediately before the ad is requested. Changing them after the banner has loaded has no effect until a new slot is composed.

Each callback receives the underlying `StroeerBannerView`, so the full View API — `adSize`, `refresh()` — remains available.

### 6.2. Reserve Space for the Banner

The banner only requests an ad once it is visible, and visibility is measured as a fraction of the node's area. A node with zero width or zero height is never visible, so it never loads.

> [!IMPORTANT]
> Always give the banner a non-zero size. `Modifier.fillMaxWidth().height(50.dp)` is a safe starting point.

```kotlin
StroeerBanner(
    adSlotId = "b1",
    modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
)
```

### 6.3. Lazy Loading and Visibility

The banner view is created when the composable enters composition, but the ad is **not** requested until the visible area of the node reaches `visibilityThreshold`. In a `LazyColumn`, banners below the fold stay unloaded until the user scrolls to them.

The wrapper requests an ad at most once per banner instance. Configured auto-refresh and explicit `refresh()` calls still apply.

> [!NOTE]
> A banner that is completely off screen never loads, even with `visibilityThreshold = 0f`. Some part of it must be on screen.

### 6.4. Adjust the Height After Loading

The creative size is known only after the ad loads. Reserve a plausible height, then snap to the real one in `onLoaded`:

```kotlin
var bannerHeight by remember(slotId) { mutableStateOf(50.dp) }

StroeerBanner(
    adSlotId = slotId,
    modifier = Modifier
        .fillMaxWidth()
        .height(bannerHeight),
    contentUrl = "https://www.example.com",
    customTargeting = mapOf("placementName" to "composeDemo"),
    onLoaded = { banner ->
        val height = banner.adSize.height
        if (height > 0) {
            bannerHeight = height.dp
        }
    },
)
```

Keying the remembered height on the slot id resets it when the slot changes.

> [!NOTE]
> The wrapper does not collapse the banner when a request fails. The space reserved by your `modifier` stays occupied. Collapse the slot or substitute your own content in `onFailed` if required.

> [!IMPORTANT]
> The size of some banners may change when an impression occurs, as their final dimensions are determined later in the rendering process.  
> If the banner is not displayed at the correct size, update the container size in the `onAdImpression` callback using the banner's latest `adSize`.


## 7. Interstitial Ads

### 7.1. StroeerInterstitial

```kotlin
@Composable
fun StroeerInterstitial(
    adSlotId: String,
    onLoaded: () -> Unit = {},
    onFailed: (StroeerException?) -> Unit = {},
    onClicked: () -> Unit = {},
    onDismissed: () -> Unit = {},
    onFailedToShow: (StroeerException?) -> Unit = {},
    onImpression: () -> Unit = {},
    onShowed: () -> Unit = {},
): StroeerInterstitialController
```

| Callback | Fires when |
| --- | --- |
| `onLoaded` | The ad is loaded and `show()` is safe to call. |
| `onFailed` | The request failed, including no fill. |
| `onClicked` | The ad was clicked. |
| `onDismissed` | The user closed the ad and returned to your app. |
| `onFailedToShow` | `show()` was called but the ad could not be presented. |
| `onImpression` | An impression was recorded. |
| `onShowed` | The ad was presented. |

The composable emits no UI node and takes no `Modifier`. It requests the ad as soon as it enters composition, and returns a controller whose `show()` presents it.

> [!IMPORTANT]
> The ad never shows itself. Nothing appears until you call `show()` on the returned controller, and only after `onLoaded` has fired. Calling `show()` earlier does nothing.

### 7.2. Load and Show Pattern

Because the ad is requested on entering composition, mount the composable only when you actually want to load — not unconditionally.

```kotlin
enum class AdState { IDLE, LOADING, READY }

var state by remember { mutableStateOf(AdState.IDLE) }
var controller: StroeerInterstitialController? by remember { mutableStateOf(null) }
// Bumping the key remounts the composable so a failed load can be retried.
var loadKey by remember { mutableStateOf(0) }

if (state != AdState.IDLE) {
    key(loadKey) {
        controller = StroeerInterstitial(
            adSlotId = "interstitial",
            onLoaded = { state = AdState.READY },
            onFailed = { state = AdState.IDLE },
            onDismissed = { state = AdState.IDLE },
            onFailedToShow = { state = AdState.IDLE },
        )
    }
}

Button(
    enabled = state != AdState.LOADING,
    onClick = {
        when (state) {
            AdState.IDLE -> { loadKey++; state = AdState.LOADING }
            AdState.READY -> { state = AdState.IDLE; controller?.show() }
            AdState.LOADING -> Unit
        }
    },
) {
    Text(
        when (state) {
            AdState.IDLE -> "Load"
            AdState.LOADING -> "Loading"
            AdState.READY -> "Show"
        },
    )
}
```

> [!WARNING]
> A loaded ad that leaves composition before `show()` is destroyed. Keep the composable mounted until the ad has been shown or dismissed.

## 8. Rewarded Ads

### 8.1. StroeerRewarded

```kotlin
@Composable
fun StroeerRewarded(
    adSlotId: String,
    onLoaded: (RewardedAd?) -> Unit = {},
    onFailed: (StroeerException) -> Unit = {},
    onUserEarnedReward: (RewardItem?) -> Unit = {},
    onClicked: () -> Unit = {},
    onDismissed: () -> Unit = {},
    onFailedToShow: (StroeerException?) -> Unit = {},
    onImpression: () -> Unit = {},
    onShowed: () -> Unit = {},
): StroeerRewardedController
```

Rewarded ads behave like interstitials — no UI node, loaded on entering composition, shown through the returned controller — with one additional callback.

| Callback | Fires when |
| --- | --- |
| `onUserEarnedReward` | The user completed the rewarded interaction and earned the reward. |

The remaining callbacks match the interstitial table in [7.1](#71-stroeerinterstitial).

> [!NOTE]
> `onLoaded` means `show()` is safe to call. Treat the `RewardedAd?` it receives as informational; it can be `null`.

### 8.2. Granting the Reward

```kotlin
StroeerRewarded(
    adSlotId = "rewarded",
    onLoaded = { state = AdState.READY },
    onFailed = { state = AdState.IDLE },
    onUserEarnedReward = { item ->
        grantReward(item?.type, item?.amount)
    },
    onDismissed = { state = AdState.IDLE },
    onFailedToShow = { state = AdState.IDLE },
)
```

> [!IMPORTANT]
> Grant the reward in `onUserEarnedReward` only. `onDismissed` fires whether or not the user watched the ad to completion.

## 9. Lifecycle

- Each composable is keyed on `adSlotId`. Changing the slot id destroys the current ad and creates a new one for the new slot.
- The underlying ad view is destroyed automatically when the composable leaves composition. **Never call `destroy()` yourself.**
- Callback lambdas may be replaced on recomposition without recreating the ad or triggering a reload.
- A banner scrolled far enough out of a `LazyColumn` to be disposed is destroyed. Scrolling back creates a new banner and requests a new ad.

## 10. Error Handling

Not every failure reaches a callback. The table below lists what to expect.

| Situation | Result |
| --- | --- |
| Unknown ad slot id | `onFailed` |
| Configuration not loaded yet | `onFailed` |
| No fill or ad server error | `onFailed` |
| A load is already in progress | `onFailed` |
| No Activity behind the Compose context | **No callback.** The failure is logged only. |
| Ad slot inactive in the remote configuration | **No callback.** The failure is logged only. |
| Blank `adSlotId` on interstitial or rewarded | **No callback.** The load is skipped; `show()` does nothing. |

> [!WARNING]
> Do not build a state machine that can only leave its loading state from a callback. In the silent cases above no callback arrives and the UI would wait forever. Add a timeout, or a way for the user to cancel.

Banner callbacks are skipped when the SDK reports no banner instance, so do not assume exactly one banner callback per request.

## 11. Differences from the View API

| | View API | Compose API |
| --- | --- | --- |
| Creation | You construct and retain the view | The composable owns it |
| Destruction | You call `destroy()` | Automatic on leaving composition |
| Banner loading | Immediate | Deferred until visible |
| Listeners | Listener interfaces | Individual lambda parameters |
| Showing full-screen ads | `loadAfterReady` may auto-show | Always manual, through the controller |
| Banner size | Read from the view | Delivered to `onLoaded` |
