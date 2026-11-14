# Yyang Kohii Adoptable Tracker (Android project)

This is an Android Studio WebView wrapper around the supplied offline-first HTML tracker. It uses AndroidX WebViewAssetLoader with a stable HTTPS-style local asset origin so WebView local storage and IndexedDB can persist more reliably than with a raw file:// URL. The original tracker UI and record logic are kept in `app/src/main/assets/index.html`.

## Included
- Updated About section with the supplied wording.
- Local records/settings/profile data using WebView local storage.
- Proof attachments using IndexedDB.
- JSON backup/restore (including proof attachments where available) and CSV export.
- Native Android file picker for importing JSON files.
- Native save-to-Downloads bridge for generated JSON/CSV/receipt files on Android 10+.
- Android print dialog bridge for receipt printing / PDF output.
- Carrd feedback link opens externally.

## Build an APK with GitHub Actions (no APK converter)
1. Upload the contents of this project folder to a new GitHub repository.
2. Open the repository's **Actions** tab and enable GitHub Actions if prompted.
3. Choose **Build Adoptable Tracker APK** and select **Run workflow** to build a test APK.
4. When the workflow finishes, open its run and download the `adoptable-tracker-debug-apk` artifact.
5. To publish a GitHub Release automatically, create and push a version tag such as `v1.0.0`. The workflow attaches `app-debug.apk` to that release.

The workflow currently creates a **debug-signed APK for testing**. Before a public production release, configure a private Android release signing key in GitHub Actions secrets and build a signed release APK. Do not commit a signing key or passwords to the repository.

## Build an APK with Android Studio
1. Open this folder in Android Studio (recent version).
2. Allow Android Studio to download the Android Gradle Plugin and SDK platform 35 if prompted.
3. Wait for Gradle sync to finish.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio will show the generated APK location. Use **Build > Generate Signed Bundle / APK** for a release APK you intend to distribute.

The project uses Android Gradle Plugin 8.6.1 and Java. A local APK could not be compiled in this environment because the Android SDK and Gradle build installation are not present. This folder is the Android Studio source project and includes a GitHub Actions workflow to build the APK remotely.

## Storage and backup
Data is stored inside this app's WebView storage on the device. It does not sync to GitHub or other devices. Uninstalling the app, clearing app storage, or resetting the device may erase it. Export JSON backups regularly and keep them private because they may contain buyer information and purchase proofs.

## Before publishing
Test on the target Android device: creating/editing/deleting records, proof image and PDF attachments, backup export/import (including proofs), CSV export, receipt printing/PDF, app restart persistence, and reinstall behavior. APK signing is required for a distributable release. GitHub Releases hosts the finished APK; it does not compile the project.

Permanent creator credit: **Made by yyang.kohii ♡**
