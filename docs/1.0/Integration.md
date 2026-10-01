# Android StröerSDK Integration Manual

## Contents

- [1. Before You Start](#1-before-you-start)
- [2. Try the Example App](#2-try-the-example-app)
- [3. SDK Environment](#3-sdk-environment)
- [4. Set Up the Application Name](#4-set-up-the-application-name)
- [5. Request a Banner Ad](#5-request-a-banner-ad)
- [6. Request Interstitial Ads (Full-Screen Ads)](#6-request-interstitial-ads-full-screen-ads)
- [7. Rewarded Ads](#7-rewarded-ads)
- [8. Ad Targeting](#8-ad-targeting)
- [9. Enable ID5](#9-enable-id5)
- [10. Clear Configuration Cache](#10-clear-configuration-cache)
- [11. Banner Ad Auto-Refresh](#11-banner-ad-auto-refresh)
- [12. Disable Error Logging](#12-disable-error-logging)
- [13. Enable Debug Mode](#13-enable-debug-mode)
- [14. Debug Info Panel](#14-debug-info-panel)

> [!IMPORTANT]
> We have rebranded the Yieldlove SDK and changed the artifact repository.  
> Please read the installation manual carefully and follow the updated integration instructions.

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

## 1. Before You Start

Before integrating the SDK, you need the following values:

- `APPLICATION_NAME` — A unique application identifier assigned to your app.
- `PUBLISHER_CALL_STRING` — Identifies the ad slot used when requesting an ad through the SDK.

Please contact your **Ströer account manager** to obtain your `APPLICATION_NAME` and `PUBLISHER_CALL_STRING`.

If you would like to start testing before your ad slots are fully configured, we can provide a **test configuration**.

The `PUBLISHER_CALL_STRING` typically uses values such as `b1`, `b2`, `b3`, or `interstitial`, depending on your configuration.

It can also be combined with a **ZONE**, which allows ad servers and SSPs (Supply-Side Platforms) to organize and serve ads based on the context in which the ad appears.

Examples:

- `home_b1` — Ad slot `b1` in the `home` zone
- `content_b1` — Ad slot `b1` in the `content` zone

## 2. Try the Example App

Start with the [native example app](https://github.com/stroeersdk/android/tree/main/example/native).

The example app includes a preconfigured test setup, so you can run and explore the SDK before receiving your own `APPLICATION_NAME` and `PUBLISHER_CALL_STRING`.

It also demonstrates the key implementation details of the SDK. Once you receive your configuration, you can update the example app with your own values and test your integration.

## 3. SDK Environment

### 3.1. Android SDK Version

The minimum supported Android SDK version is **24**. The SDK is built with **Target SDK 36**.

Make sure your project's `minSdk` is set to `24` or higher.

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
android {
    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }
}
```

**Groovy (`build.gradle`)**

```groovy
android {
    defaultConfig {
        minSdk 24
        targetSdk 36
    }
}
```

### 3.2. Java Version 17

The SDK requires **Java 17**. Configure your project to use Java 17.

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
```

**Groovy (`build.gradle`)**

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
```

### 3.3. Kotlin Version

The SDK uses **Kotlin 2.1.0** as its minimum supported Kotlin version.

Make sure your project uses Kotlin `2.1.0` or later.

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
plugins {
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
}
```

**Groovy (`build.gradle`)**

```groovy
plugins {
    id 'org.jetbrains.kotlin.android' version '2.1.0' apply false
}
```

If your project still manages Kotlin through the root `buildscript` block:

**Kotlin DSL**

```kotlin
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}
```

**Groovy**

```groovy
buildscript {
  dependencies {
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0"
  }
}
```
### 3.4. Gradle and Android Gradle Plugin

The following are the minimum supported versions:

- **Gradle:** `8.11.1`
- **Android Gradle Plugin (AGP):** `8.10.1`

You may use newer compatible versions. Using older versions may cause build or compatibility issues.

**Gradle Wrapper (`gradle-wrapper.properties`)**

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-all.zip
```

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
plugins {
    id("com.android.application") version "8.10.1" apply false
}
```

**Groovy (`build.gradle`)**

```groovy
plugins {
    id 'com.android.application' version '8.10.1' apply false
}
```

### 3.5. Add the SDK Repository

Add the StröerSDK Maven repository to your project.

For modern Android projects, repositories are typically configured in `settings.gradle` or `settings.gradle.kts`.

#### Kotlin DSL (`settings.gradle.kts`)

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

#### Groovy (`settings.gradle`)

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

#### Legacy Projects

For legacy projects that configure repositories in the project-level `build.gradle`, add the repository to the `allprojects` block.

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
allprojects {
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

**Groovy (`build.gradle`)**

```groovy
allprojects {
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
### 3.6. Add SDK Dependencies

Add the required StröerSDK modules to your app-level Gradle file.

Replace `<version>` with the SDK version you want to use.

#### Kotlin DSL (`build.gradle.kts`)

```kotlin
dependencies {
    implementation("com.stroeer.ads:core:<version>")
    implementation("com.stroeer.ads:cmp:<version>")
    implementation("com.stroeer.ads:confiant:<version>")
}
```

#### Groovy (`build.gradle`)

```groovy
dependencies {
    implementation "com.stroeer.ads:core:<version>"
    implementation "com.stroeer.ads:cmp:<version>"
    implementation "com.stroeer.ads:confiant:<version>"
}
```
## 4. Set Up the Application Name

Before requesting an ad, set the `APPLICATION_NAME` provided by Ströer.

The application name only needs to be set **once**, preferably when your application starts. Pass the application context as the first parameter.

```kotlin
StroeerSDK.setApplicationName(
    applicationContext,
    "APPLICATION_NAME"
)
```

> **Important**  
> You do not need to set the application name again for each ad request.

---

## 5. Request a Banner Ad

### 5.1. Create a BannerView

Create a `StroeerBannerView` and call `load()` with your `PUBLISHER_CALL_STRING`.

Banner dimensions and custom targeting parameters are retrieved dynamically from the remote configuration.

A `StroeerBannerListener` can optionally be provided to receive banner lifecycle events. If you do not need callbacks, pass `null`.

```kotlin
import com.stroeer.ads.formats.banner.StroeerBannerView

val bannerView = StroeerBannerView(this@BannerActivity)

// Attach the banner to the view hierarchy.
parentView.addView(bannerView)

// If you do not need banner callbacks, pass null.
bannerView.load(PUBLISHER_CALL_STRING, null)
```

The `StroeerBannerView` should be attached to the view hierarchy while the ad is loading. This allows the SDK to validate and render the HTML creative correctly.

> [!IMPORTANT]
> We recommend attaching the `StroeerBannerView` before calling `load()`.  
> If the view is added only after an ad has loaded successfully, some debug information may not be available when the banner fails to render, which can make troubleshooting more difficult.

### 5.2. Banner Listener

`StroeerBannerListener` provides default empty implementations of the banner callbacks.

You only need to override the callbacks required by your application.

Available callbacks include:

- `onAdLoaded` — Called when the banner has loaded successfully.
- `onAdFailedToLoad` — Called when the banner fails to load.
- `onAdOpened` — Called when the ad opens an overlay.
- `onAdClosed` — Called when the overlay is closed.
- `onAdClicked` — Called when the user clicks the banner.
- `onAdImpression` — Called when an impression is recorded.

Example:

```kotlin
import android.util.Log
import com.stroeer.ads.exceptions.StroeerException
import com.stroeer.ads.formats.banner.StroeerBannerListener
import com.stroeer.ads.formats.banner.StroeerBannerView

class MyBannerListener : StroeerBannerListener() {

    override fun onAdLoaded(banner: StroeerBannerView?) {
        Log.d("StroeerBanner", "Banner successfully loaded.")
    }

    override fun onAdFailedToLoad(
        banner: StroeerBannerView?,
        error: StroeerException
    ) {
        Log.e("StroeerBanner", "Banner failed to load: ${error.message}")
    }

    override fun onAdOpened(banner: StroeerBannerView?) {
        Log.d("StroeerBanner", "Ad opened.")
    }

    override fun onAdClosed(banner: StroeerBannerView?) {
        Log.d("StroeerBanner", "Ad closed.")
    }

    override fun onAdClicked(banner: StroeerBannerView?) {
        Log.d("StroeerBanner", "Banner clicked.")
    }

    override fun onAdImpression(banner: StroeerBannerView?) {
        Log.d("StroeerBanner", "Impression recorded.")
    }
}
```

If you only need specific events, override only those callbacks:

```kotlin
class MyBannerListener : StroeerBannerListener() {

    override fun onAdLoaded(banner: StroeerBannerView?) {
        Log.d("StroeerBanner", "Banner loaded!")
    }

    override fun onAdFailedToLoad(
        banner: StroeerBannerView?,
        error: StroeerException
    ) {
        Log.e("StroeerBanner", "Banner failed to load: ${error.message}")
    }
}
```

Each callback receives the corresponding `StroeerBannerView`. This allows the same listener instance to be reused across multiple banner views if required.

### 5.3. Complete Example

The following example demonstrates a basic banner integration inside an activity. Store the banner as an activity property so it can be released in `onDestroy()`. This example assumes that the application name has already been set during application startup.

```kotlin
private var bannerView: StroeerBannerView? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_banner)

    val adContainer: ViewGroup = findViewById(R.id.adContainer)

    val newBannerView = StroeerBannerView(this)
    bannerView = newBannerView

    val listener = object : StroeerBannerListener() {

        override fun onAdLoaded(banner: StroeerBannerView?) {
            // Banner loaded successfully.
        }

        override fun onAdFailedToLoad(
            banner: StroeerBannerView?,
            error: StroeerException
        ) {
            // Handle the error.
        }

        override fun onAdOpened(banner: StroeerBannerView?) {
        }

        override fun onAdClosed(banner: StroeerBannerView?) {
        }

        override fun onAdClicked(banner: StroeerBannerView?) {
        }

        override fun onAdImpression(banner: StroeerBannerView?) {
        }
    }

    // Attach the banner before loading.
    adContainer.removeAllViews()
    adContainer.addView(newBannerView)

    // Replace with your PUBLISHER_CALL_STRING.
    newBannerView.load("home_b1", listener)
}
```

### 5.4. Destroy the Banner

Call `destroy()` when the banner is no longer needed.

This releases resources associated with the banner, stops refresh operations, removes listeners, and helps prevent memory leaks.

Once a banner has been destroyed, the instance must not be reused.

```kotlin
bannerView.destroy()
```

For example, you can destroy the banner when the Activity is destroyed:

```kotlin
override fun onDestroy() {
    bannerView?.destroy()
    bannerView = null
    super.onDestroy()
}
```

#### Automatically Destroy When Detached

If you want the banner to be destroyed automatically when it is detached from the view hierarchy, enable:

```kotlin
bannerView.destroyWhenDetached = true
```

This can be useful when banners are dynamically added and removed during layout changes or navigation.

> [!CAUTION]
> **LeakCanary**
>
> When profiling your application with LeakCanary, you may see retained references involving banner views or related ad components.
>
> Some of these references can be caused by internal caching in the Google Mobile Ads SDK or Prebid Mobile SDK, where views or related objects may be retained temporarily for reuse.
>
> A retained reference reported by LeakCanary does not necessarily indicate a permanent memory leak. Make sure that `destroy()` is called when the banner is no longer required.

---

### 5.5. Retrieve the Ad Size

The actual banner size may not be known until the creative has loaded.

Use `adSize` after `onAdLoaded()` when you need to adjust the container layout or determine the dimensions of the loaded banner.

```kotlin
override fun onAdLoaded(banner: StroeerBannerView?) {
    val adSize = banner?.adSize

    Log.i("StroeerBanner",
        "${banner?.publisherSlotName} - ${adSize?.width} x ${adSize?.height}"
    )
}
```

> [!IMPORTANT]
> The size of some banners may change when an impression occurs, as their final dimensions are determined later in the rendering process.  
> If the banner is not displayed at the correct size, update the container size in the `onAdImpression` callback using the banner's latest `adSize`.
> ```kotlin
> override fun onAdImpression(banner: StroeerBannerView?) {
>    val adSize = banner?.adSize
>
>    Log.i("StroeerBanner",
>        "${banner?.publisherSlotName} - ${adSize?.width} x ${adSize?.height}"
>    )
> }
> ```

## 6. Request Interstitial Ads (Full-Screen Ads)

Interstitial ads are full-screen ads typically displayed at natural transition points in an application, such as between game levels, between articles, or during navigation.

Unlike banner ads, interstitial ads cover the application interface until the user dismisses or interacts with the ad.

### 6.1. Create and Load an Interstitial Ad

To load an interstitial ad, create a `StroeerInterstitialView` and call `load()` with your `PUBLISHER_CALL_STRING`.

Because an interstitial may take some time to load, we recommend preloading it before it is needed. The `onAdLoaded()` callback indicates that the interstitial is ready to be displayed.

```kotlin
import com.stroeer.ads.exceptions.StroeerException
import com.stroeer.ads.formats.interstitial.StroeerInterstitialListener
import com.stroeer.ads.formats.interstitial.StroeerInterstitialView

val interstitialAd = StroeerInterstitialView(this)

// By default, the interstitial is shown automatically when it is ready.
// Set this to false before loading if you want to control when it is shown.
interstitialAd.loadAfterReady = false

interstitialAd.load(
    PUBLISHER_CALL_STRING,
    object : StroeerInterstitialListener() {

        override fun onAdLoaded() {
            // The interstitial is ready to be displayed.
            interstitialAd.show()
        }

        override fun onAdFailedToLoad(exception: StroeerException?) {
            Log.e(
                "StroeerInterstitial",
                "Failed to load interstitial: ${exception?.message}"
            )
        }
    }
)
```

### 6.2. `loadAfterReady`

The `loadAfterReady` property controls whether the interstitial is displayed automatically after it has finished loading.

**`true` (default)**

The interstitial is displayed automatically after the `onAdLoaded()` callback.

```kotlin
interstitialAd.loadAfterReady = true
```

**`false`**

The interstitial is loaded but not displayed automatically.

```kotlin
interstitialAd.loadAfterReady = false
```

Call `show()` when you are ready to display the ad:

```kotlin
override fun onAdLoaded() {
    interstitialAd.show()
}
```

This is useful when you need full control over when the interstitial appears, for example after a game level ends or following a specific user action.

> [!IMPORTANT]
> When `loadAfterReady` is set to `false`, call `show()` only after `onAdLoaded()` has been triggered.

### 6.3. Interstitial Listeners

The SDK provides two listener classes for handling interstitial events:

- `StroeerInterstitialListener` — Provides the essential loading callbacks.
- `StroeerInterstitialFullListener` — Provides the complete set of interstitial lifecycle callbacks.

Both listener classes provide default empty implementations, so you only need to override the callbacks required by your application.

#### Basic Listener

Use `StroeerInterstitialListener` when you only need to handle loading success or failure.

```kotlin
class MyInterstitialListener : StroeerInterstitialListener() {

    override fun onAdLoaded() {
        Log.d(
            "StroeerInterstitial",
            "Interstitial successfully loaded."
        )
    }

    override fun onAdFailedToLoad(exception: StroeerException?) {
        Log.e(
            "StroeerInterstitial",
            "Failed to load interstitial: ${exception?.message}"
        )
    }
}
```

#### Full Listener

Use `StroeerInterstitialFullListener` when you need to handle the complete interstitial lifecycle.

```kotlin
class MyInterstitialFullListener : StroeerInterstitialFullListener() {

    override fun onAdLoaded() {
        Log.d(
            "StroeerInterstitial",
            "Interstitial successfully loaded."
        )
    }

    override fun onAdFailedToLoad(exception: StroeerException?) {
        Log.e(
            "StroeerInterstitial",
            "Failed to load interstitial: ${exception?.message}"
        )
    }

    override fun onAdShowedFullScreenContent() {
        Log.d(
            "StroeerInterstitial",
            "Interstitial displayed."
        )
    }

    override fun onAdFailedToShowFullScreenContent(
        exception: StroeerException?
    ) {
        Log.e(
            "StroeerInterstitial",
            "Failed to show interstitial: ${exception?.message}"
        )
    }

    override fun onAdDismissedFullScreenContent() {
        Log.d(
            "StroeerInterstitial",
            "Interstitial dismissed."
        )
    }

    override fun onAdClicked() {
        Log.d(
            "StroeerInterstitial",
            "Interstitial clicked."
        )
    }

    override fun onAdImpression() {
        Log.d(
            "StroeerInterstitial",
            "Interstitial impression recorded."
        )
    }
}
```

### 6.4. Destroy the Interstitial

Call `destroy()` when the interstitial is no longer needed to release its resources.

```kotlin
interstitialAd.destroy()
```

Once an interstitial has been destroyed, the instance must not be reused.

To display another interstitial, create a new `StroeerInterstitialView` instance and call `load()` again.

> [!IMPORTANT]
> After an interstitial has been shown and dismissed, call `destroy()` before discarding the instance.

## 7. Rewarded Ads

A rewarded ad is a full-screen ad that allows users to receive a reward in exchange for interacting with the ad.

Typical rewards include in-game currency, extra lives, or access to premium content.

Rewarded ads are generally user-initiated. For example, an application may display a **Watch Ad to Earn Reward** button and show the rewarded ad after the user chooses to proceed.

### 7.1. Create and Load a Rewarded Ad

To load a rewarded ad, create a `StroeerRewardedView` and call `load()` with your `PUBLISHER_CALL_STRING`.

Because a rewarded ad may take some time to load, we recommend preloading it before it is needed. The `onAdLoaded()` callback indicates that the rewarded ad is ready to be displayed.

```kotlin
import com.stroeer.ads.exceptions.StroeerException
import com.stroeer.ads.formats.rewarded.StroeerRewardedListener
import com.stroeer.ads.formats.rewarded.StroeerRewardedView

val rewardedAdView = StroeerRewardedView(this)

// By default, the rewarded ad is shown automatically when it is ready.
// Set this to false before loading if you want to control when it is shown.
rewardedAdView.loadAfterReady = false

rewardedAdView.load(
    PUBLISHER_CALL_STRING,
    object : StroeerRewardedListener() {

        override fun onAdLoaded(rewardedAd: RewardedAd?) {
            // The rewarded ad is ready to be displayed.
            rewardedAdView.show()
        }

        override fun onAdFailedToLoad(exception: StroeerException) {
            Log.e(
                "StroeerRewarded",
                "Failed to load rewarded ad: ${exception.message}"
            )
        }

        override fun onUserEarnedReward(item: RewardItem?) {
            // Grant the reward to the user.
        }
    }
)
```

### 7.2. `loadAfterReady`

The `loadAfterReady` property controls whether the rewarded ad is displayed automatically after it has finished loading.

**`true` (default)**

The rewarded ad is displayed automatically after it has finished loading.

```kotlin
rewardedAdView.loadAfterReady = true
```

**`false`**

The rewarded ad is loaded but not displayed automatically.

```kotlin
rewardedAdView.loadAfterReady = false
```

Call `show()` when you are ready to display the ad:

```kotlin
override fun onAdLoaded(rewardedAd: RewardedAd?) {
    rewardedAdView.show()
}
```

This is useful when the application needs to wait for a specific user action before displaying the ad, such as tapping a **Watch Ad to Earn Reward** button.

> [!IMPORTANT]
> When `loadAfterReady` is set to `false`, call `show()` only after `onAdLoaded()` has been triggered.

### 7.3. Rewarded Listeners

The SDK provides two listener classes for handling rewarded ad events:

- `StroeerRewardedListener` — Provides the essential loading and reward callbacks.
- `StroeerRewardedFullListener` — Provides the complete set of rewarded ad lifecycle callbacks.

Both listener classes provide default empty implementations, so you only need to override the callbacks required by your application.

#### Basic Listener

Use `StroeerRewardedListener` when you only need the essential loading and reward callbacks.

```kotlin
class MyRewardedListener : StroeerRewardedListener() {

    override fun onAdLoaded(rewardedAd: RewardedAd?) {
        Log.d(
            "StroeerRewarded",
            "Rewarded ad successfully loaded."
        )
    }

    override fun onAdFailedToLoad(exception: StroeerException) {
        Log.e(
            "StroeerRewarded",
            "Failed to load rewarded ad: ${exception.message}"
        )
    }

    override fun onUserEarnedReward(item: RewardItem?) {
        Log.d(
            "StroeerRewarded",
            "Reward earned: ${item?.type} ${item?.amount}"
        )

        // Grant the reward to the user here.
    }
}
```

> [!IMPORTANT]
> Grant the reward in `onUserEarnedReward()`. Do not use `onAdDismissedFullScreenContent()` to determine whether the user earned a reward.

#### Full Listener

Use `StroeerRewardedFullListener` when you need to handle the complete rewarded ad lifecycle.

```kotlin
class MyRewardedFullListener : StroeerRewardedFullListener() {

    override fun onAdLoaded(rewardedAd: RewardedAd?) {
        Log.d(
            "StroeerRewarded",
            "Rewarded ad successfully loaded."
        )
    }

    override fun onAdFailedToLoad(exception: StroeerException) {
        Log.e(
            "StroeerRewarded",
            "Failed to load rewarded ad: ${exception.message}"
        )
    }

    override fun onAdShowedFullScreenContent() {
        Log.d(
            "StroeerRewarded",
            "Rewarded ad displayed."
        )
    }

    override fun onAdFailedToShowFullScreenContent(
        exception: StroeerException?
    ) {
        Log.e(
            "StroeerRewarded",
            "Failed to show rewarded ad: ${exception?.message}"
        )
    }

    override fun onAdDismissedFullScreenContent() {
        Log.d(
            "StroeerRewarded",
            "Rewarded ad dismissed."
        )
    }

    override fun onAdClicked() {
        Log.d(
            "StroeerRewarded",
            "Rewarded ad clicked."
        )
    }

    override fun onAdImpression() {
        Log.d(
            "StroeerRewarded",
            "Rewarded ad impression recorded."
        )
    }

    override fun onUserEarnedReward(item: RewardItem?) {
        Log.d(
            "StroeerRewarded",
            "Reward earned: ${item?.type} ${item?.amount}"
        )

        // Grant the reward to the user here.
    }
}
```

### 7.4. Destroy the Rewarded Ad

Call `destroy()` when the rewarded ad is no longer needed to release its resources.

```kotlin
rewardedAdView.destroy()
```

Once a rewarded ad has been destroyed, the instance must not be reused.

To display another rewarded ad, create a new `StroeerRewardedView` instance and call `load()` again.

> [!IMPORTANT]
> After a rewarded ad has been shown and dismissed, call `destroy()` before discarding the instance.

## 8. Ad Targeting

Ad targeting provides contextual and audience information with ad requests. This can improve ad relevance and may improve advertising performance.

### 8.1. Content URL

Use `contentUrl` to specify the URL of the content the user is currently viewing. This gives Google and other advertising services additional context about the page’s topic or category.

```kotlin
StroeerSDK.setContentUrl("https://www.stroeer.de")
```

This is especially useful for apps that display articles, product pages, or other web-based content.

### 8.2. Custom Targeting

Use the `customTargeting` map to provide additional information with ad requests.

Custom targeting supports the following categories:

- **`context`:** Topics or categories describing the app or current content, such as sports or entertainment.
- **`user`:** Audience information, such as interests, preferences, or location-related values.
- **Custom key-value pairs:** Additional information about a page, screen, or content section—for example, `section` with the value `soccer`.

#### Global Targeting

Global targeting applies across ad slots and formats. Set these values early in the app lifecycle, preferably during initialization, so they are available for subsequent ad requests.

Global targeting uses a map of keys to lists of string values:

```kotlin
val customTargeting: MutableMap<String, List<String>> = HashMap()

// Describe the app or current content.
customTargeting["context"] = listOf("sport", "game", "technology")

// Provide audience information.
customTargeting["user"] = listOf("sports", "technology")

// Add a custom key-value pair.
customTargeting["section"] = listOf("soccer")

Stroeer.setGlobalCustomTargeting(customTargeting)
```

### 8.3. Local Targeting

Local targeting lets you configure targeting for an individual banner.

Unlike global targeting, local targeting uses string values. In the example below, multiple values are provided as comma-separated strings:

```kotlin
val bannerView = StroeerBannerView(this@BannerActivity)

bannerView.bannerConfig.customTargeting = mutableMapOf(
    "context" to "sport,game,technology",
    "user" to "sports,technology",
    "section" to "soccer"
)
```

> [!NOTE]
> When local targeting is configured, it takes precedence over global targeting for that banner.

## 9. Enable ID5

The SDK supports ID5, which is disabled by default. To enable it, contact your Ströer account manager.

No code changes are required to use ID5. However, you can optionally provide additional user information to improve identification. Refer to the ID5 website for details.

You can provide this information at any time. StroeerSDK automatically detects updates and refreshes the ID5 identifier. For best results, provide the information before initializing the SDK, where possible.

> [!IMPORTANT]
> ID5 requires the applicable user consent. If the user opts out of ID5 in their privacy settings, the SDK will not use ID5.

```kotlin
val customInfo = IdentityCustomInfo().apply {
    email = "customeremail@domain.com"
    phone = "phonenumber"
    puid = "ID"
    regionCode = "RegionalCode"
    cityCode = "CityCode"
}

IdentityManager.getInstance().setCustomInfo(customInfo)
```

### Custom Information

Providing `CustomInfo` is optional. You can supply only the fields available to you.

- **`regionCode`:** Use an ISO 3166-2 subdivision code. For locations in the United States, use the two-letter state code.
- **`cityCode`:** Use the United Nations Code for Trade and Transport Locations (UN/LOCODE).

## 10. Clear Configuration Cache

The SDK caches configuration associated with your application name and updates it periodically.

To request the latest configuration immediately, clear the cache. Reloading may take approximately 5–10 seconds.

```kotlin
StroeerSDK.clearConfigurationCache(activity)
```

## 11. Banner Ad Auto-Refresh

Banner ads can refresh automatically at a configured interval, such as every 30 seconds. To enable this feature, contact your Ströer account manager.

## 12. Disable Error Logging

SDK error logs may add noise to your app’s monitoring and reporting. Set `disableErrorLog` to `true` to log these messages at the informational level instead of the error level.

```kotlin
ConfigurationManager.disableErrorLog = true
```

> [!NOTE]
> This setting changes the log level; it does not suppress the messages.

## 13. Enable Debug Mode

Debug mode provides additional information for testing and troubleshooting. Once enabled, it remains active until the app is restarted.

Use the following methods to enable verbose logging or inspection mode:

```kotlin
// Enable verbose logging.
StroeerSDK.enableDebugMode()

// Enable the debug panel and detailed logging.
StroeerSDK.enableInspectionMode()
```

## 14. Debug Info Panel

To activate ad debugging mode, press and hold an ad with two or three fingers for 3–4 seconds, until a notification appears.

Once debugging mode is active:

1. A debug label appears in the banner’s top-left corner.
2. Tap the label to open the ad’s debug information panel.
3. Double-tap anywhere on the panel to copy its contents.
4. Force-close and reopen the app to exit debugging mode.

You can also enable the panel programmatically:

```kotlin
StroeerSDK.enableInspectionMode()
```

> [!WARNING]
> Inspection mode is intended for ad debugging. Do not enable it by default in production builds.
