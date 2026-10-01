# Confiant Plugin Integration Manual for Android

## Contents

- [1. Overview](#1-overview)
- [2. Before You Start](#2-before-you-start)
- [3. Try the Example App](#3-try-the-example-app)
- [4. Add SDK Dependencies](#4-add-sdk-dependencies)
    - [4.1. Configure Repositories](#41-configure-repositories)
    - [4.2. Add Dependencies](#42-add-dependencies)
- [5. Initialize Confiant](#5-initialize-confiant)
    - [Initialization Parameters](#initialization-parameters)
    - [Ad Reloading](#ad-reloading)
- [6. Enable Test Mode](#6-enable-test-mode)
 
## 1. Overview

The Confiant plugin integrates ad monitoring and blocking into your Android app through StröerSDK.

To enable Confiant, contact your Ströer account manager or the Ströer support team.

## 2. Before You Start

Complete the main Android SDK integration before adding the Confiant plugin.

Obtain your Confiant property ID from the Ströer support team or the Confiant team. You will need this value during initialization.

## 3. Try the Example App

Use the [native example app](https://github.com/stroeersdk/android/tree/main/example/native) to explore the integration before configuring Confiant in your application.

## 4. Add SDK Dependencies

### 4.1. Configure Repositories

Add the following repositories to your existing Gradle repository configuration.

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

        maven {
            url = uri("https://cdn.confiant-integrations.net/backend-integrations/in-app/releases/android/maven")
            content {
                includeGroup("com.confiant.android")
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

        maven {
            url 'https://cdn.confiant-integrations.net/backend-integrations/in-app/releases/android/maven'
            content {
                includeGroup 'com.confiant.android'
            }
        }
    }
}
```

### 4.2. Add Dependencies

Add the following dependencies to your app-level Gradle file. Replace `<version>` with your StröerSDK version, using the same version for the `core` and `confiant` modules.

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
dependencies {
    implementation("com.stroeer.ads:core:<version>")
    implementation("com.stroeer.ads:confiant:<version>")
    implementation("com.confiant.android:sdk:6.1.4")
}
```

**Groovy (`build.gradle`)**

```groovy
dependencies {
    implementation 'com.stroeer.ads:core:<version>'
    implementation 'com.stroeer.ads:confiant:<version>'
    implementation 'com.confiant.android:sdk:6.1.4'
}
```

> [!NOTE]
> If the `core` dependency is already declared, do not add it again. Use the Confiant SDK version supported by your StröerSDK release.

## 5. Initialize Confiant

Obtain the singleton `ConfiantLoader` instance and call `initialize()` with your property ID, the reload setting, and an initialization callback.

```kotlin
ConfiantLoader.getInstance().initialize(
    "confiantPropertyId", // Replace with your Confiant property ID.
    false,               // Keep automatic reloading disabled.
    object : IAdMonitorCallback {
        override fun onInitialized(isInitialized: Boolean) {
            if (isInitialized) {
                // Initialization succeeded.
            } else {
                // Handle initialization failure.
            }
        }
    }
)
```

### Initialization Parameters

| Parameter | Description |
| --- | --- |
| `confiantPropertyId` | Your Confiant property ID, provided by Ströer or Confiant. |
| `enableReload` | Controls whether blocked ads should be reloaded. Keep this set to `false` while reloading is disabled. |
| `IAdMonitorCallback` | Receives the initialization result through `onInitialized(isInitialized: Boolean)`. |

### Ad Reloading

> [!WARNING]
> Automatic reloading is currently disabled because it may cause app crashes. Set `enableReload` to `false`.

When supported, the reload setting determines how blocked ads are handled:

- **`true`:** Attempts to reload the ad up to two times.
- **`false`:** Blocks the ad without attempting to reload it.

## 6. Enable Test Mode

To test ad blocking, enable test mode:

```kotlin
ConfiantLoader.getInstance().enableTestMode()
```

In test mode, Confiant blocks all ads.

> [!WARNING]
> Test mode is intended only for development and testing. Do not enable it in production builds.

> [!NOTE]
> Confiant monitoring and blocking do not apply to video ads.