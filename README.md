# 🗺️ Travel Distance Measurer

<div align="center">

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34-brightgreen.svg)

**A modern Android app for tracking trips with real-time GPS, background tracking, and beautiful map visualization**

</div>

---

## 📱 Overview

Travel Distance Measurer is an Android application that tracks your trips using GPS technology. Track your walks, runs, or drives with real-time distance calculation, background tracking, and route visualization on Google Maps.

## 🎯 Key Features

- **Real-time GPS tracking** with live distance and duration
- **Background tracking** - continues when app is minimized or screen is off
- **Route visualization** on Google Maps with polylines
- **Trip history** - view, edit, and delete past trips
- **Persistent notifications** with trip progress
- **Local database storage** - all data stored on device

---

## 🚀 Prerequisites

| Requirement | Version |
|-------------|---------|
| **Android Studio** | Ladybug 2024.2.1 or higher |
| **JDK** | 17 or higher |
| **Android SDK** | Min API 24, Target API 34 |
| **Google Maps API Key** | Required |

---

## 🛠️ Setup & Build Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Kovvuri-Chandra-Kiran-Reddy/TravelDistanceMeasurer.git
cd TravelDistanceMeasurer
```

### 2. Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select existing)
3. Enable **Maps SDK for Android**
4. Navigate to **Credentials** → **Create Credentials** → **API Key**
5. Copy the API key

### 3. Configure API Key

Create or edit `local.properties` in the project root:

```properties
sdk.dir=/path/to/Android/Sdk
MAPS_API_KEY=YOUR_API_KEY_HERE
```

⚠️ **Note**: `local.properties` is gitignored - never commit this file!

### 4. Build the Project

**Using Android Studio:**
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click **Run** (Shift+F10)

**Using Command Line:**
```bash
# Build debug APK
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

### 5. Run the App

1. Connect an Android device or start an emulator
2. Run the app from Android Studio or install the APK
3. **Grant location permission** when prompted
4. **Grant notification permission** (Android 13+) when prompted
5. Tap **Start** to begin tracking

---

## 📋 Required Permissions

The app requires the following permissions:

- **Location** (Fine & Coarse) - Required for GPS tracking
- **Notification** (Android 13+) - Required for background tracking
- **Background Location** (Optional) - For tracking with screen off

All permissions are requested at runtime when needed.

---

## 🔧 Tech Stack

- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: SQLDelight
- **Maps**: Google Maps Compose
- **Location**: Play Services Location

---

<div align="center">

Made with ❤️ using Modern Android Development

</div>
