# Ströer SDK for Android

The Ströer SDK helps publishers integrate banner, interstitial, and rewarded ads into Android applications. Optional modules add consent management (CMP), Jetpack Compose wrappers, and ad-quality protection with Confiant.


## Release Notes (v1.0.0)
### New

- Rebranded the SDK as **StröerSDK**.
- Changed the package name to `com.stroeer.ads`.
- Updated the library repository: [GitHub – StröerSDK Android](https://github.com/stroeersdk/android)
- Added a Jetpack Compose wrapper (`com.stroeer.compose`).
- Configuration is now revalidated using an **ETag** instead of being re-downloaded on every refresh.

### Fixed

- Corrected the precedence of key-values and `contentUrl` for GAM. Key-values from remote configuration are now also included in Prebid requests.
- Ad size is now rechecked at impression time, as the size of some banners is determined only when the impression occurs.
- Fixed various minor bugs.
- The SDK no longer adds an app-name label to the merged manifest.

### Changed

- The SDK version reported to GAM and Prebid now follows the new versioning scheme (`major + 20`).
- Removed the **OkHttp** dependency.
- SDK libraries are now obfuscated.
- Updated third-party library versions:
  - **Prebid Mobile:** `3.3.4`
  - **SourcePoint:** `7.15.13`
  - **Google Mobile Ads SDK:** `24.6.0` (publishers can upgrade to a later version)
  - **Kotlin:** `2.1.0` (publishers can upgrade to a later version)

> [!IMPORTANT]
> The Yieldlove SDK has been rebranded as **StröerSDK**. The package name is now `com.stroeer.ads` and the artifact repository has changed. Please follow the updated installation instructions below.


## Documentation

- [Integration manual](docs/1.0/Integration.md) — setup, banner, interstitial, rewarded, targeting, debugging
- [Jetpack Compose integration manual](docs/1.0/Compose_Integration.md)
- [CMP integration manual](docs/1.0/CMP_Integration.md)
- [Confiant integration manual](docs/1.0/Confiant_Integration.md)

Example applications in this repository:

- [Native (View-based) example](example/native)
- [Jetpack Compose example](example/compose)

Both examples ship with a preconfigured test setup, so you can run them before receiving your own credentials.

## Requirements

| | Version |
| --- | --- |
| `minSdk` | 24 |
| `targetSdk` | 36 |
| Java | 17 |
| Kotlin | 2.1.0 or newer |
| Gradle | 8.11.1 or newer |
| Android Gradle Plugin | 8.10.1 or newer |

The SDK is compiled against Kotlin 2.1.0. Newer Kotlin versions generally work, but the Kotlin metadata version of your app must stay compatible with the SDK and with Google Mobile Ads.

## Before you start

You need two values from your **Ströer account manager**:

- `APPLICATION_NAME` — a unique identifier for your app.
- `PUBLISHER_CALL_STRING` — identifies the ad slot used for a request, for example `b1`, `interstitial`, or a zone-prefixed slot such as `home_b1`.

A test configuration is available if you want to start before your own ad slots are configured.

## Installation

### 1. Add the SDK repository

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

Legacy projects can add the same `maven { ... }` block to `allprojects.repositories` in the project-level `build.gradle`.

### 2. Add the SDK modules

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
dependencies {
    implementation("com.stroeer.ads:core:<version>")
    implementation("com.stroeer.ads:cmp:<version>")
    implementation("com.stroeer.ads:compose:<version>")
    implementation("com.stroeer.ads:confiant:<version>")
}
```

**Groovy (`build.gradle`)**

```groovy
dependencies {
    implementation 'com.stroeer.ads:core:<version>'
    implementation 'com.stroeer.ads:cmp:<version>'
    implementation 'com.stroeer.ads:compose:<version>'
    implementation 'com.stroeer.ads:confiant:<version>'
}
```

| Module | Required | Purpose |
| --- | --- | --- |
| `core` | Yes | Banner, interstitial, and rewarded ads |
| `cmp` | No | SourcePoint consent management wrapper |
| `compose` | No | Jetpack Compose wrappers for all ad formats |
| `confiant` | No | Confiant ad-quality monitoring |

Use the same `<version>` for all modules. Declare `core` explicitly even when using `compose` or `confiant`: those modules depend on `core` at runtime scope, so it is not on your compile classpath automatically.

Confiant additionally requires the Confiant Maven repository and SDK — see the [Confiant integration manual](docs/1.0/Confiant_Integration.md).

Any TCF-compliant CMP works without the `cmp` module — the SDK reads the standard `IABTCF_TCString` consent value from `SharedPreferences`.

## Basic setup

Set the application name once, preferably from your `Application` class. Pass the application context:

```kotlin
StroeerSDK.setApplicationName(applicationContext, "APPLICATION_NAME")
```

This starts an asynchronous configuration fetch. Ads requested before the configuration arrives fail with a configuration error.

## Banner example

Attach the banner to a parent view before calling `load()`, so the SDK can validate and render the creative and so debug information stays available when rendering fails.

```kotlin
private var bannerView: StroeerBannerView? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_banner)

    val adContainer: ViewGroup = findViewById(R.id.adContainer)
    val banner = StroeerBannerView(this)
    bannerView = banner

    adContainer.addView(banner)

    banner.load("home_b1", object : StroeerBannerListener() {
        override fun onAdLoaded(banner: StroeerBannerView?) {
            // The banner is ready.
        }

        override fun onAdFailedToLoad(
            banner: StroeerBannerView?,
            error: StroeerException,
        ) {
            // Handle the loading failure.
        }
    })
}
```

Pass `null` instead of a listener if you do not need callbacks. Release the banner when it is no longer needed — a destroyed instance must not be reused:

```kotlin
override fun onDestroy() {
    bannerView?.destroy()
    bannerView = null
    super.onDestroy()
}
```

The final banner size may only be known once an impression occurs; read `banner.adSize` in `onAdLoaded` and again in `onAdImpression` if you size the container yourself.

## Interstitial example

Full-screen ads need an `Activity` context. Set `loadAfterReady` to `false` before loading to control when the ad is shown:

```kotlin
val interstitial = StroeerInterstitialView(this).apply { loadAfterReady = false }

interstitial.load("AD_SLOT_ID", object : StroeerInterstitialListener() {
    override fun onAdLoaded() {
        interstitial.show()
    }

    override fun onAdFailedToLoad(exception: StroeerException?) {
        // Handle the loading failure.
    }
})
```

Use `StroeerInterstitialFullListener` for the complete lifecycle (shown, dismissed, clicked, impression, failed-to-show). Call `destroy()` once the ad has been shown and dismissed.

## Rewarded example

```kotlin
val rewarded = StroeerRewardedView(this).apply { loadAfterReady = false }

rewarded.load("AD_SLOT_ID", object : StroeerRewardedListener() {
    override fun onAdLoaded(rewardedAd: RewardedAd?) {
        rewarded.show()
    }

    override fun onAdFailedToLoad(exception: StroeerException) {
        // Handle the loading failure.
    }

    override fun onUserEarnedReward(item: RewardItem?) {
        grantReward(item?.type, item?.amount)
    }
})
```

Grant the reward in `onUserEarnedReward()` only — never in `onAdDismissedFullScreenContent()`. `StroeerRewardedFullListener` exposes the full lifecycle. As with interstitials, call `destroy()` once the ad has been shown and dismissed; destroyed instances must not be reused.

## Jetpack Compose

The `compose` module provides `StroeerBanner`, `StroeerInterstitial`, and `StroeerRewarded`. The composables own the underlying ad view: it is created on entering composition and destroyed on leaving it, so never call `destroy()` yourself. Banners load lazily, requesting an ad only once they are actually visible.

```kotlin
StroeerBanner(
    adSlotId = "b1",
    modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
)
```

Always give the banner non-zero size — a node with zero width or height is never visible and never loads. See the [Compose integration manual](docs/1.0/Compose_Integration.md) for the full-screen formats, controllers, and error handling.

## Consent

With the `cmp` module, collect consent through `StroeerConsent`:

```kotlin
val consent = StroeerConsent(this, R.id.main)

consent.collect()
```

`collect()` also takes an optional `MessageLanguage`. That type — like the listener callback types — comes from SourcePoint, which the `cmp` module depends on at runtime scope only. To reference it, add SourcePoint to your compile classpath:

```kotlin
implementation("com.sourcepoint.cmplibrary:cmplibrary:7.15.13")
```

Calling `collect()` again does not redisplay the message to a user who already gave consent, so it is safe to call at startup. Use `showPrivacyManager()` to let users review or change their choices. See the [CMP integration manual](docs/1.0/CMP_Integration.md) for listeners, authenticated consent, and PUR layouts.

## Confiant

With the optional `confiant` module, initialise it once with your property ID:

```kotlin
ConfiantLoader.getInstance().initialize(
    "confiantPropertyId",
    false, // Automatic reloading is currently disabled.
    object : IAdMonitorCallback {
        override fun onInitialized(isInitialized: Boolean) {
            // Handle the initialization result.
        }
    }
)
```

Confiant monitoring does not apply to video ads. See the [Confiant integration manual](docs/1.0/Confiant_Integration.md).

## Targeting

```kotlin
// Content currently shown to the user.
StroeerSDK.setContentUrl("https://www.stroeer.de")

// Global targeting, applied to all slots and formats.
// Set this early, preferably during app initialization.
StroeerSDK.setGlobalCustomTargeting(
    mapOf(
        "context" to listOf("sport", "game", "technology"),
        "user" to listOf("sports", "technology"),
        "section" to listOf("soccer"),
    )
)

// Per-banner targeting, set before load(). Takes precedence over
// global targeting for that banner, and uses comma-separated strings.
val banner = StroeerBannerView(this)

banner.bannerConfig.customTargeting = mutableMapOf(
    "context" to "sport,game,technology",
    "section" to "soccer",
)
```

## Debugging

```kotlin
StroeerSDK.enableDebugMode()      // Verbose logging.
StroeerSDK.enableInspectionMode() // Debug panel and detailed logging.

// Log SDK errors at info level instead of error level.
ConfigurationManager.disableErrorLog = true
```

You can also open the debug panel on a device: press and hold an ad with two or three fingers for 3–4 seconds, then tap the debug label in the banner's top-left corner. Double-tap the panel to copy its contents, and restart the app to leave debugging mode.

> [!WARNING]
> Inspection mode is intended for ad debugging. Do not enable it in production builds.

## Support

For onboarding, production configuration, or integration support, contact your Ströer account manager.
