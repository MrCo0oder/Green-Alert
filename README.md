# 🟢 Green Alert

**Location-Based Arrival Alert App for Android**

Never miss your stop again! Green Alert notifies you when you arrive at your predefined destinations - even when your phone is in your pocket.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 📍 **Pick from Map** | Tap anywhere on OpenStreetMap to set a destination |
| ✏️ **Manual Entry** | Type an address and geocode it automatically |
| 📤 **Share from Maps** | Share locations directly from Google Maps |
| 🔔 **Smart Alerts** | Sound + vibration notifications on arrival |
| ⚙️ **Customizable Radius** | Set alert radius per destination (50m - 1000m) |
| 🔄 **Background Tracking** | Works even when the app is closed |
| 🔋 **Battery Efficient** | Uses geofencing instead of continuous GPS |

---

## 📱 Screenshots

*Coming soon*

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room
- **Preferences**: DataStore
- **Maps**: OpenStreetMap (osmdroid)
- **Location**: Google Play Services Geofencing API

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 24+ (minSdk)
- Device with Google Play Services

### Build & Run
```bash
# Clone the repository
git clone https://github.com/yourusername/GreenAlert.git

# Open in Android Studio and sync Gradle

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## 📋 Permissions

The app requires the following permissions:

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | Precise location for geofencing |
| `ACCESS_BACKGROUND_LOCATION` | Track location when app is closed |
| `POST_NOTIFICATIONS` | Show arrival alerts (Android 13+) |
| `FOREGROUND_SERVICE` | Keep location service running |
| `VIBRATE` | Vibration alerts |
| `INTERNET` | Load map tiles |

---

## 📂 Project Structure

```
app/src/main/java/com/example/greenalert/
├── data/
│   ├── local/          # Room database
│   ├── model/          # Data classes
│   ├── preferences/    # DataStore
│   └── repository/     # Repository layer
├── di/                 # Hilt modules
├── service/            # Location & Geofencing services
├── ui/
│   ├── components/     # Reusable UI components
│   ├── home/           # Home screen
│   ├── mappicker/      # Map picker screen
│   ├── manualinput/    # Manual address entry
│   ├── navigation/     # Navigation graph
│   ├── settings/       # Settings screen
│   └── theme/          # Material theme
└── util/               # Utilities
```

---

## 🧪 Testing

### Test Geofencing on Emulator
1. Open Android Emulator
2. Go to **Extended Controls** → **Location**
3. Set coordinates matching a saved destination
4. Verify notification appears

---

## 📄 License

This project is licensed under the MIT License.

---

## 🤝 Contributing

Contributions are welcome! Please open an issue or submit a pull request.

---

**Made with ❤️ using Kotlin & Jetpack Compose**
