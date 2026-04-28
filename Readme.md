# TruthLens — AI Image Detection App

**Module:** CIS4034-N Mobile App Development  
**Student:** Araoye Adedamola  
**Student Number:** e4615842  
**University:** Teesside University

---

## Overview

TruthLens is an Android application that uses AI to detect whether an image is real or AI-generated. It integrates with the Hive AI Detection API (V3) to analyse images captured via camera or uploaded from the device gallery, returning a confidence score, generator attribution (e.g. Midjourney, Stable Diffusion), and deepfake risk assessment. All results are stored locally using Room Database for offline history access.

---

## Features

- Firebase email/password authentication (register, login, logout)
- Animated splash screen on launch
- Capture images using the device camera
- Upload images from the device gallery
- AI detection via Hive AI V3 API with:
    - AI vs Real confidence score
    - Generator attribution (Midjourney, DALL·E, Stable Diffusion, Adobe Firefly, Google Imagen)
    - Deepfake risk level (Low / Medium / High)
- Scan history screen with local Room Database persistence
- User profile screen showing scan statistics from Firebase + Room DB
- Network connectivity check before API calls
- Full error handling with user-friendly messages

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Authentication | Firebase Authentication |
| Local Database | Room (SQLite) |
| Networking | Retrofit 2 + Gson |
| Image Loading | Coil |
| AI Detection | Hive AI API V3 |
| Build System | Gradle (Kotlin DSL) |

---

## Requirements

- Android Studio Hedgehog or later
- Android SDK 34
- Minimum Android version: API 24 (Android 7.0)
- A Hive AI Playground API key — obtain one at https://thehive.ai/explore?api_keys=1
- A Firebase project with Email/Password authentication enabled

---

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/adeymoe/TruthLens.git
cd TruthLens
```

### 2. Configure Firebase

1. Go to [https://console.firebase.google.com](https://console.firebase.google.com)
2. Create a new project (or use an existing one)
3. Add an Android app with package name `uk.ac.tees.mad.e4615842`
4. Download the `google-services.json` file
5. Place it in the `app/` directory:
```
TruthLens/
└── app/
    └── google-services.json   ← place here
```
6. In the Firebase console, go to **Authentication → Sign-in method** and enable **Email/Password**

### 3. Configure the Hive API Key

1. Obtain a Playground Secret Key from [https://thehive.ai/explore?api_keys=1](https://thehive.ai/explore?api_keys=1)
2. Open `local.properties` in the project root (create it if it does not exist)
3. Add your key on a new line:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
HIVE_API_KEY=your_actual_key_here
```

### 4. Open in Android Studio

1. Open Android Studio
2. Select **File → Open** and navigate to the project root
3. Wait for Gradle sync to complete
4. If prompted, accept any SDK or dependency updates

### 5. Build and Run

1. Select a device
2. Click the green **Run** button
3. The app will build, install, and launch automatically

---

## Agile Development

This project was developed using an Agile methodology across 5 sprints, tracked via Trello and Git.

**Trello Board:** https://trello.com/invite/b/69afff019b412964c521fb6f/ATTI2349fac1ef171b8580b82348ea89e3ac4719D8D4/truthlens

**Git Repository:** https://github.com/adeymoe/TruthLens


---

## Security Notes

- The Hive API key is stored in `local.properties` and injected at build time via `BuildConfig`. It is never hardcoded in source files or committed to version control.
- `google-services.json` is excluded from version control via `.gitignore`.
- Firebase Authentication enforces authenticated access — no screen is reachable without a valid session.
- All API communication uses HTTPS exclusively.
- The app requests only the minimum necessary Android permissions: `INTERNET`, `CAMERA`, `READ_MEDIA_IMAGES`, `ACCESS_NETWORK_STATE`.
- Scan history is stored locally on the user's device in a private Room database, not shared externally.

---


## Contact

**Student:** Araoye Adedamola  
**Email:** e4615842@tees.ac.uk  
**Module:** CIS4034-N Mobile App Development  
**Tutor:** Julien Cordry