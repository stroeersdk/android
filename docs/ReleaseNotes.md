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