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
- Java 17
- Kotlin 2.1.0 or newer
- Gradle 8.11.1 or newer
- Android Gradle Plugin 8.11.1 or newer

Refer to the integration guide for the currently supported `compileSdk`, `targetSdk`, dependency versions, and third-party SDK compatibility.

## Installation

The SDK is distributed through [GitHub Packages](https://github.com/orgs/stroeersdk/packages). GitHub requires authentication when downloading Maven packages, including public packages.

### 1. Add the GitHub Packages repository

Add the following repository to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "StroeerGitHubPackages"
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
            name = 'StroeerGitHubPackages'
            url = uri('https://maven.pkg.github.com/stroeersdk/android')
        }
    }
}
```

### 3. Add the SDK modules

Add the modules you need to your application module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.stroeer.ads:core:<SDK_VERSION>")
    implementation("com.stroeer.ads:cmp:<SDK_VERSION>")
    implementation("com.stroeer.ads:confiant:<SDK_VERSION>")
}
```

Or with Groovy DSL in `build.gradle`:

```groovy
dependencies {
    implementation 'com.stroeer.ads:core:<SDK_VERSION>'
    implementation 'com.stroeer.ads:cmp:<SDK_VERSION>'
    implementation 'com.stroeer.ads:confiant:<SDK_VERSION>'
}
```

`core` is required. Add `cmp` and `confiant` only when those features are needed. Replace `<SDK_VERSION>` with a version published on the repository's **Packages** page.

In GitHub Actions, `GITHUB_ACTOR` and `GITHUB_TOKEN` can be used instead of local Gradle properties when the workflow repository has permission to read the package.

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

For interstitial, rewarded, targeting, consent, privacy, debugging, and lifecycle examples, see the [complete integration guide](https://stroeerdigitalgroup.atlassian.net/wiki/spaces/SDGPUBLIC/pages/1890648275/Android+integration+documentation).

## Support

For onboarding details, production configuration, or integration support, contact your Ströer account manager.
