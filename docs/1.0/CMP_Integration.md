# Consent Management Platform(CMP) Integration Manual

## 1. Overview

The SDK includes a Consent Management Platform (CMP) for collecting and managing user consent. It implements the Transparency and Consent Framework (TCF) v2.

The generated Transparency and Consent (TC) string is available to your application in the standard storage location defined by the TCF v2 specification.

## 2. Add the SDK Dependency

Add the CMP dependency to your module’s `dependencies` block, replacing `<version>` with the SDK version you want to use:

```groovy
implementation 'com.stroeer.ads:cmp:<version>'
```

## 4. Before You Start

Obtain your `APPLICATION_NAME` from your publisher account manager. You will use this value to initialize the SDK.

## 5. Set the Application Name

Set `APPLICATION_NAME` once, before using the consent APIs. Pass the application context as the first argument:

```kotlin
StroeerSDK.setApplicationName(
    getApplicationContext(),
    "APPLICATION_NAME"
)
```

## 6. Collect and Manage Consent

### 6.1. Simple Consent Message

The Simple Consent Message explains how user data is collected and processed. It typically includes **Accept All** and **Manage My Options** buttons. Button labels may vary by configuration and language.

To collect consent, create a `StroeerConsent` instance and call `collect()`:

```kotlin
val consent = StroeerConsent(
    this,
    R.id.main
)

consent.collect(MessageLanguage.ENGLISH)
```

In this example:

- `this` is the activity context.
- `R.id.main` identifies the main layout of your activity.
- The message language is optional. Call `consent.collect()` to omit it.

> [!NOTE]
> If the user has already provided consent, calling `collect()` again does not redisplay the Simple Consent Message. You can therefore call it at app startup without first checking for existing consent. To let users review or change their choices, open the Privacy Manager.
>
> Whether the consent dialog appears also depends on the geographic targeting configured in Sourcepoint. For example, users outside Europe may not see the dialog if it is configured to appear only in European regions.

### 6.2. Privacy Manager

The Privacy Manager lets users review individual purposes and vendors and update their consent choices. It is also accessible through the **Manage My Options** button in the Simple Consent Message.

To open it, create a `StroeerConsent` instance and call `showPrivacyManager()`:

```kotlin
val consent = StroeerConsent(
    this,
    R.id.main
)

consent.showPrivacyManager(MessageLanguage.ENGLISH)
```

If a named Privacy Manager layout is configured in your consent account, pass its name:

```kotlin
consent.showPrivacyManager(
    "Your-Privacy-Manager-Layout-Name",
    MessageLanguage.ENGLISH
)
```

> [!NOTE]
> The message language is optional for both methods.
### 6.3. Handle Consent Events and Errors

Use `StroeerConsentListener` to respond to consent events. It is an open class that implements `IStroeerConsentListener` and provides default implementations, so you only need to override the callbacks your application requires.

>[!NOTE]
>#### Required SourcePoint Dependency
> The listener callbacks use Sourcepoint types, including SPConsents and ConsentAction. Add the Sourcepoint CMP dependency to your app-level Gradle file:
>```kotlin
>dependencies {
>    implementation("com.sourcepoint.cmplibrary:cmplibrary:7.15.13")
>}
>```

The listener callbacks reference Sourcepoint types, including `SPConsents` and `ConsentAction`. These types must also be available on your app’s compile classpath. If the CMP module does not expose Sourcepoint transitively, add an explicit dependency on the Sourcepoint CMP library version used by that module.

When using the listener-based constructor, add and remove the consent view through the UI callbacks.

| Callback | When it is called | Action |
| --- | --- | --- |
| `onConsentUIReady` | The consent view is ready to display. | Add the view to your layout. |
| `onConsentUIFinished` | The consent view is ready to close. | Remove the view from its parent. |
| `onConsentReady` | Consent data is ready. | Handle the supplied `SPConsents` object. |
| `onError` | An error occurs during the consent flow. | Handle the nullable `Throwable`. |
| `onAction` | A consent action occurs. | Handle and return the `ConsentAction`. |

The following example manages the consent view and handles consent data, errors, and actions:

```kotlin
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.sourcepoint.cmplibrary.model.ConsentAction
import com.sourcepoint.cmplibrary.model.exposed.SPConsents
import com.stroeer.cmp.StroeerConsentListener

val consent = StroeerConsent(
    this,
    object : StroeerConsentListener() {
        override fun onConsentUIReady(v: View) {
            val parent = findViewById<ViewGroup>(R.id.main)

            if (v.parent == null) {
                parent.addView(
                    v,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }

            v.bringToFront()
            v.requestLayout()
        }

        override fun onConsentUIFinished(v: View) {
            (v.parent as? ViewGroup)?.removeView(v)
        }

        override fun onConsentReady(c: SPConsents) {
            // Handle the available consent data.
        }

        override fun onError(v: Throwable?) {
            Log.e("StroeerConsent", "Consent flow failed.", v)
        }

        override fun onAction(
            view: View,
            consentAction: ConsentAction
        ): ConsentAction {
            // Handle the action if needed.
            return consentAction
        }
    }
)

consent.collect()
```

In this example, `this` is the activity context and `R.id.main` identifies the layout that hosts the consent view.

> [!IMPORTANT]
> Callback names begin with lowercase `on`. Because `StroeerConsentListener` is a class, use `object : StroeerConsentListener()` with parentheses.
>
> If you override `onAction()`, return a `ConsentAction`. The default implementation returns the received action unchanged.

### 6.4. Set Consent Programmatically

The SDK exposes Sourcepoint’s `customConsentTo()` functionality to update the current user’s consent for specified vendors, categories, and legitimate-interest categories.

Two overloads are available.

**With an `OnConsentReadyCallback`:**

```kotlin
fun customConsentTo(
    vendors: ArrayList<String>,
    categories: ArrayList<String>,
    legIntCategories: ArrayList<String>,
    onCustomConsentReady: OnConsentReadyCallback
)
```

**With a `CustomConsentListener`:**

```kotlin
fun customConsentTo(
    vendors: ArrayList<String>,
    categories: ArrayList<String>,
    legIntCategories: ArrayList<String>,
    customConsentListener: CustomConsentListener
)
```

### 6.5. Authenticated Consent

To use Authenticated Consent, pass an `authId` when creating the `StroeerConsent` instance:

```kotlin
val consent = StroeerConsent(
    this,
    R.id.main,
    "<authId>"
)

consent.collect()
```

Replace `<authId>` with your authentication identifier.

### 6.6. PUR and Custom Consent Layouts

To display a PUR or another configured consent layout, pass its `layoutName` to `collect()` or `showPrivacyManager()`.

Obtain the correct layout name from the publisher account manager who provided your `APPLICATION_NAME`.

```kotlin
// Example only: replace with your configured layout name.
val layoutName = "purLayer"

val consent = StroeerConsent(
    this,
    object : StroeerConsentListener {
        // Implement the required listener methods.
    }
)

// Option 1: Collect consent using a custom layout.
consent.collect(layoutName)

// Option 2: Open the Privacy Manager using a custom layout.
consent.showPrivacyManager(layoutName)
```

Choose the method appropriate to your flow; the two calls above illustrate alternatives.

## 7. Clear Stored Consent

To clear previously stored consent, call `clearConsent()`:

```kotlin
val consent = StroeerConsent(
    this,
    R.id.main
)

consent.clearConsent()
```