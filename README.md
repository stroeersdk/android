# Ströer SDK for Android

The Ströer SDK helps publishers integrate banner, interstitial, and rewarded ads into Android applications. Optional modules are available for consent management (CMP), ad-quality protection with Confiant, and additional demand integrations.

## Documentation

- [Android integration guide](https://stroeerdigitalgroup.atlassian.net/wiki/spaces/SDGPUBLIC/pages/1890648275/Android+integration+documentation)
- [CMP integration guide](https://stroeerdigitalgroup.atlassian.net/wiki/spaces/SDGPUBLIC/pages/2452030603/Android+CMP)
- [Confiant integration guide](https://stroeerdigitalgroup.atlassian.net/wiki/spaces/SDGPUBLIC/pages/4915626047/Confiant+Plugin+Documentation+for+Android)
- [Release notes](https://stroeerdigitalgroup.atlassian.net/wiki/spaces/SDGPUBLIC/pages/5104205845/Release+Notes+Android)
- [Example application](https://github.com/mbrtargeting/stroeerSDK-android-example)

## Requirements

- Android `minSdk` 24 or newer
- `compileSdk` 36
- Java 17
- Kotlin 2.1.0
- Gradle 8.11.1 or newer
- Android Gradle Plugin 8.10.1 or newer

The SDK is compiled against Kotlin 2.1.0. Newer Kotlin versions generally work, but the Kotlin metadata version of your app must stay compatible with the SDK and with Google Mobile Ads.

Refer to the integration guide for the currently supported `targetSdk`, dependency versions, and third-party SDK compatibility.

## Installation

The SDK is distributed through [GitHub Packages](https://github.com/orgs/stroeersdk/packages).

### 1. Add the GitHub Packages repository

Add the following repository to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "StroeerSDK"
            url = uri("https://maven.pkg.github.com/stroeersdk/android")
        }
    }
}
```

For Groovy DSL, add this to `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = 'StroeerSDK'
            url = uri('https://maven.pkg.github.com/stroeersdk/android')
        }
    }
}
```

### 2. Add the SDK modules

Add the modules you need to your application module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.stroeer.ads:core:<SDK_VERSION>")
    implementation("com.stroeer.ads:cmp:<SDK_VERSION>")
    implementation("com.stroeer.ads:confiant:<SDK_VERSION>")
    implementation("com.stroeer.ads:compose:<SDK_VERSION>")
}
```

Or with Groovy DSL in `build.gradle`:

```groovy
dependencies {
    implementation 'com.stroeer.ads:core:<SDK_VERSION>'
    implementation 'com.stroeer.ads:cmp:<SDK_VERSION>'
    implementation 'com.stroeer.ads:confiant:<SDK_VERSION>'
    implementation 'com.stroeer.ads:compose:<SDK_VERSION>'
}
```

| Module | Required | Purpose |
| --- | --- | --- |
| `core` | Yes | Banner, interstitial, and rewarded ads |
| `cmp` | No | SourcePoint consent management wrapper |
| `confiant` | No | Confiant ad-quality monitoring |
| `compose` | No | Jetpack Compose wrappers for all ad formats |

Replace `<SDK_VERSION>` with a version published on the repository's **Packages** page.

Any TCF-compliant CMP works without the `cmp` module — the SDK reads the standard `IABTCF_TCString` consent value from `SharedPreferences`.

## Basic setup

Set the application name once, preferably from your `Application` class:

```kotlin
StroeerSDK.setApplicationName(applicationContext, "APPLICATION_NAME")
```

## Banner example

Create the banner, start loading, and immediately attach it to a parent view so the SDK can validate and render its content:

```kotlin
val banner = StroeerBannerView(this)

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

adContainer.addView(banner)
```

Release the banner when it is no longer needed:

```kotlin
banner.destroy()
```

## Interstitial example

Interstitials must be created with an `Activity` context. Set `loadAfterReady` to `false` to control when the ad is shown:

```kotlin
val interstitial = StroeerInterstitialView(this).apply { loadAfterReady = false }

interstitial.load("AD_SLOT_ID", object : StroeerInterstitialFullListener() {
    override fun onAdLoaded() {
        interstitial.show()
    }

    override fun onAdFailedToLoad(exception: StroeerException?) {
        // Handle the loading failure.
    }
})
```

## Rewarded example

```kotlin
val rewarded = StroeerRewardedView(this).apply { loadAfterReady = false }

rewarded.load("AD_SLOT_ID", object : StroeerRewardedFullListener() {
    override fun onAdLoaded(rewardedAd: RewardedAd?) {
        rewarded.show()
    }

    override fun onUserEarnedReward(item: RewardItem?) {
        grantReward(item?.type, item?.amount)
    }
})
```

## Confiant

When the optional `confiant` module is present, initialise it once with your property ID:

```kotlin
ConfiantLoader.getInstance().initialize("YOUR_CONFIANT_PROPERTY_ID")
```

If the module is not on the classpath the call is a no-op and ad loading continues normally.

For Jetpack Compose, targeting, consent, privacy, debugging, and lifecycle examples, see the [complete integration guide](https://stroeerdigitalgroup.atlassian.net/wiki/spaces/SDGPUBLIC/pages/1890648275/Android+integration+documentation).

## Support

For onboarding details, production configuration, or integration support, contact your Ströer account manager.
