# IPTV App for Android TV

A modern, high-performance IPTV client designed exclusively for Android TV and Fire TV devices. This application connects directly to Xtream Codes API providers, offering a premium, buttery-smooth viewing experience with a sleek, D-Pad optimized user interface.

## 🚀 Features

- **Xtream Codes Integration:** Secure, local authentication with support for dynamic provider URLs.
- **Media3 ExoPlayer Engine:** Robust live video playback with custom load controls (drip-feeding buffers) and WAF circumvention using custom `User-Agent` and `Referer` headers.
- **Live TV Grid:** A visually stunning, adaptive channel grid that scales dynamically upon D-Pad focus.
- **EPG TV Guide:** A lazy-loaded Electronic Program Guide with LRU caching to prevent server strain while providing instantaneous timeline navigation.
- **VOD & Catch-up TV:** A dedicated movie catalog optimized for portrait movie posters.
- **Fast FTS4 Search:** Instant, full-text channel searching powered by a reactive SQLite FTS4 shadow table.
- **Automated Data Sync:** Seamless background synchronization of your provider's live streams, VODs, and categories.

## 🛠️ Tech Stack

Built entirely with modern Android development practices:
- **UI:** Jetpack Compose for TV (`androidx.tv.material3`)
- **Video:** AndroidX Media3 (ExoPlayer)
- **Architecture:** Clean Architecture + MVVM + UDF (Unidirectional Data Flow)
- **Dependency Injection:** Dagger Hilt
- **Local Database:** Room Database with Full-Text Search (FTS4)
- **Network:** Retrofit + Gson
- **State Management:** Kotlin Coroutines & Flows
- **Security:** DataStore Preferences for credential storage

## 📺 Installation (End Users)

This app is distributed via GitHub Releases and can be sideloaded directly onto any Android TV or Fire TV stick using the **Downloader** app by AFTVnews.

1. Install **Downloader** on your TV.
2. Open Downloader and enter the URL to the latest `app-release.apk` from the [Releases](../../releases) tab.
3. Click Install and launch the app!
4. Log in with your Xtream Codes credentials (Host URL, Username, Password).

## 💻 Development & Building

### Requirements
- Android Studio Ladybug (or newer)
- JDK 17
- Min SDK: 23 (Android 6.0)
- Target SDK: 35 (Android 15)

### Building Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/gilbertoHennepin/IPTV-App.git
   ```
2. Open the project in Android Studio.
3. Build and deploy to an Android TV emulator or physical device.

### CI/CD Pipeline
This repository includes a GitHub Actions workflow that automatically builds and signs a release APK whenever a new `v*` tag is pushed. 

To utilize the automated release pipeline, configure the following **GitHub Actions Secrets**:
- `KEYSTORE_BASE64`: A Base64-encoded version of your `.jks` signing keystore.
- `KEYSTORE_PASSWORD`: The password for your keystore.
- `KEY_ALIAS`: Your signing key alias.
- `KEY_PASSWORD`: The password for your key alias.

When you are ready to publish a release, tag your commit and push it:
```bash
git tag v1.0.8
git push origin --tags
```
The workflow will automatically compile the APK and attach it to a new GitHub Release.
