# 🛡️ Privacy Scanner

A native Android privacy and security scanner that analyzes installed apps for dangerous permissions, privacy risks, and spyware-like behavior — **100% on-device, no data ever leaves your phone.**

---

## 📱 Features

| Feature | Details |
|---|---|
| **App Scanner** | Scans all installed apps via Android PackageManager API |
| **Risk Scoring** | Privacy risk score (0–100) per app with color coding |
| **Spyware Detection** | Detects 13 suspicious behavior patterns |
| **Permission Breakdown** | Plain-language descriptions for every permission |
| **Quick Actions** | Open Settings, Revoke Permissions, Uninstall |
| **Privacy Tips** | 10 expandable educational tip cards |
| **Dark Mode** | Full Material 3 dark/light theme support |
| **Background Monitor** | Optional monitoring for newly installed risky apps |

### Risk Levels
- 🟢 **Safe** (0–20) — Minimal permissions, no suspicious patterns
- 🟡 **Moderate** (41–60) — Some sensitive permissions
- 🔴 **High** (61–80) — Multiple dangerous permissions or suspicious combos
- ⛔ **Critical** (81–100) — Spyware-like behavior detected

### Spyware Detection Patterns
- Location + Microphone combination
- Camera + Microphone combination
- Background location access
- Contacts + SMS/MMS access
- Accessibility service abuse
- Device administrator elevation
- Auto-start on boot
- Persistent background services
- Can send SMS
- Call log access
- Full storage access
- Package installation capability
- Outdated security standards

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (ViewModel + LiveData + Repository)
- **Async:** Kotlin Coroutines + Flow
- **UI:** Material Design 3, Navigation Component, RecyclerView
- **Storage:** All local — Room-ready, no external servers
- **Security:** Network security config blocks all external traffic

---

## 🚀 Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK with API 34

### Clone & Build

```bash
git clone https://github.com/YOUR_USERNAME/PrivacyScanner.git
cd PrivacyScanner

# First time only: download real Gradle wrapper
./setup-gradle.sh

# Build debug APK
./gradlew assembleDebug

# APK location
# app/build/outputs/apk/debug/app-debug.apk
```

### Open in Android Studio
1. Open Android Studio
2. **File → Open** → select the `PrivacyScanner` folder
3. Android Studio will sync Gradle and generate the wrapper automatically
4. Click **Run** ▶️

---

## 📋 Required Permissions

| Permission | Purpose |
|---|---|
| `QUERY_ALL_PACKAGES` | Read installed app list on Android 11+ |
| `PACKAGE_USAGE_STATS` | Detect background service activity |
| `POST_NOTIFICATIONS` | Alert when risky apps are installed |
| `RECEIVE_BOOT_COMPLETED` | Restart monitoring after reboot (optional) |

> **Privacy guarantee:** The `network_security_config.xml` explicitly blocks all cleartext and external traffic. No analytics, no telemetry, no ads.

---

## 📂 Project Structure

```
PrivacyScanner/
├── app/src/main/
│   ├── java/com/privacyscanner/
│   │   ├── data/
│   │   │   ├── model/          # AppInfo, PermissionInfo, RiskLevel, SpywareFlag
│   │   │   └── repository/     # AppScannerRepository (PackageManager)
│   │   ├── ui/
│   │   │   ├── dashboard/      # DashboardFragment
│   │   │   ├── scanner/        # ScannerFragment
│   │   │   ├── appdetail/      # AppDetailFragment
│   │   │   ├── tips/           # TipsFragment, PrivacyTipsActivity
│   │   │   └── adapters/       # AppListAdapter, PermissionAdapter, TipsAdapter
│   │   ├── viewmodel/          # ScannerViewModel (MVVM)
│   │   ├── utils/              # PermissionDatabase, RiskAnalyzer
│   │   ├── service/            # PrivacyMonitorService
│   │   └── receiver/           # BootReceiver, PackageReceiver
│   └── res/
│       ├── layout/             # All XML layouts
│       ├── drawable/           # Vector icons & backgrounds
│       ├── navigation/         # Nav graph
│       └── values/             # Colors, strings, themes, dimens
└── .github/workflows/          # Android CI (GitHub Actions)
```

---

## ⚖️ License

```
Copyright 2026 Privacy Scanner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m 'Add your feature'`
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request

---

*Built with ❤️ for privacy-conscious Android users.*
