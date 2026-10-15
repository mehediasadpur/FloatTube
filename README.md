# FloatTube — Android Studio project

A Kotlin Android sample that embeds YouTube using the official YouTube IFrame Player API and requests Android Picture-in-Picture (PiP).

## Requirements
- Android Studio (current stable)
- Android SDK Platform 35
- JDK 17
- Internet connection

## Build
1. Clone or download this repository.
2. Open the repository root in Android Studio.
3. Allow Gradle sync and install SDK 35 if prompted.
4. Run on Android 8.0+ (API 26+) device/emulator.
5. To build an APK: **Build → Build APK(s)**. Output normally: `app/build/outputs/apk/debug/app-debug.apk`.

## Use
Paste a YouTube video URL (`youtube.com/watch?v=...`, `youtu.be/...`, Shorts, Live, or embed URL), tap **Load video**, use YouTube's player controls, then tap **Enter floating mode** to request PiP.

## Important limitations
- This app does not download, proxy, or extract YouTube media, remove ads, or bypass YouTube Premium/background-playback restrictions.
- PiP playback for embedded YouTube can vary by Android version, WebView, video, account, and YouTube policy. Entering PiP does not guarantee playback continues.
- Some videos disable embedding or have age/region restrictions.
- YouTube branding, controls, ads and player behavior remain controlled by YouTube.

This is an independent sample and is not affiliated with YouTube or Google.