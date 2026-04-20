# 📱 WebViewApp — Android WebView Template

A production-ready Android WebView app template.  
Convert **any website into a native Android app** in minutes.

---

## ⚡ Quick Start (3 steps)

### Step 1 — Set your URL
Open `app/src/main/java/com/webviewapp/Config.java` and change **one line**:

```java
public static final String START_URL = "https://www.yourwebsite.com";
```

### Step 2 — Add Firebase (for push notifications)
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a project → Add Android app → package: `com.webviewapp`
3. Download `google-services.json`
4. Replace `app/google-services.json` with your downloaded file

### Step 3 — Add AdMob (for ads)
1. Go to [AdMob Console](https://admob.google.com)
2. Create a new app → Android
3. Copy your **App ID** and replace in `AndroidManifest.xml`:
   ```xml
   <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID"
              android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"/>
   ```
4. Create Banner + Interstitial ad units
5. Copy the ad unit IDs into `Config.java`

**Build and run!** 🚀

---

## 📁 Project Structure

```
WebViewApp/
├── app/src/main/
│   ├── java/com/webviewapp/
│   │   ├── Config.java                  ← 🔧 ALL settings in one file
│   │   ├── SplashActivity.java          ← Animated splash screen
│   │   ├── MainActivity.java            ← Core WebView + all features
│   │   ├── WebAppInterface.java         ← JS ↔ Native bridge
│   │   └── MyFirebaseMessagingService.java ← FCM push notifications
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_splash.xml      ← Splash UI
│   │   │   └── activity_main.xml        ← Main UI (WebView + ads)
│   │   ├── values/
│   │   │   ├── strings.xml
│   │   │   ├── colors.xml               ← Brand colors
│   │   │   └── themes.xml               ← Light theme
│   │   └── values-night/
│   │       └── themes.xml               ← Dark theme (auto)
│   ├── AndroidManifest.xml
│   └── google-services.json            ← ⚠️ Replace with yours
├── app/build.gradle                    ← Dependencies
├── build.gradle
├── settings.gradle
└── app/proguard-rules.pro
```

---

## ⚙️ Config.java Reference

| Setting | Description |
|---|---|
| `START_URL` | The website URL loaded on launch |
| `ALLOWED_DOMAIN` | Domain to keep inside the app. Set `""` to allow all. |
| `ADS_ENABLED` | `true`/`false` toggle for all ads |
| `ADMOB_BANNER_ID` | Your banner ad unit ID |
| `ADMOB_INTERSTITIAL_ID` | Your interstitial ad unit ID |
| `INTERSTITIAL_EVERY_N_PAGES` | Show interstitial every N page loads |
| `FCM_DEFAULT_TOPIC` | Topic all devices auto-subscribe to |
| `SPLASH_DURATION_MS` | How long the splash screen shows (ms) |
| `USER_AGENT_SUFFIX` | Appended to WebView user-agent string |
| `ABOUT_TEXT` | Text shown in the About dialog |

---

## 🌐 JavaScript Bridge

Call native Android functions from your website's JavaScript:

```javascript
// Show a native Toast
Android.showToast("Hello from web!")

// Share sheet
Android.shareText("Check this out!", "https://example.com")

// Open phone dialer
Android.openDialer("555-1234")

// Open email client
Android.openEmail("hi@example.com", "Subject", "Body text")

// Open in external browser
Android.openBrowser("https://example.com")

// Open Google Maps
Android.openMaps("Eiffel Tower, Paris")

// Check connectivity
if (Android.isNetworkAvailable()) { ... }

// Get app info
var pkg = Android.getPackageName()
var ver = Android.getAppVersion()

// Close the app
Android.closeApp()
```

---

## 🔔 FCM Push Notifications

Send pushes from Firebase Console or your server.

**Payload format:**
```json
{
  "to": "<device_token_or_topic>",
  "notification": {
    "title": "New message!",
    "body": "You have a new update."
  },
  "data": {
    "url": "https://yourwebsite.com/specific-page"
  }
}
```

- `data.url` is **optional** — if present, the app will open that URL when the notification is tapped.
- Send to topic `/topics/all_users` to reach all subscribed devices.

---

## 💰 AdMob Setup

1. Replace test IDs in `Config.java` with production IDs.
2. Replace the App ID in `AndroidManifest.xml`.
3. **Do not use test IDs in a published app** — your account may be suspended.

To **disable all ads** (e.g. for a paid/premium version):
```java
public static final boolean ADS_ENABLED = false;
```

---

## 🎨 Customising the Splash Screen

Edit `res/values/colors.xml`:
```xml
<color name="splashBackground">#1565C0</color>  <!-- background -->
<color name="splashText">#FFFFFF</color>          <!-- app name -->
<color name="splashTextSecondary">#B3E5FC</color> <!-- tagline -->
```

Edit `res/values/strings.xml`:
```xml
<string name="app_name">My App</string>
<string name="splash_tagline">Your tagline here</string>
```

---

## 📦 Renaming the Package

To change `com.webviewapp` to your own package:

1. In Android Studio: right-click `com.webviewapp` → Refactor → Rename
2. Update `applicationId` in `app/build.gradle`
3. Update `package` in `AndroidManifest.xml`
4. Re-register your app in Firebase & AdMob with the new package name

---

## 🔒 Permissions Used

| Permission | Why |
|---|---|
| `INTERNET` | Load the website |
| `ACCESS_NETWORK_STATE` | Detect offline state |
| `CAMERA` | File upload (camera option) |
| `READ_MEDIA_IMAGES/VIDEO` | File upload from gallery |
| `POST_NOTIFICATIONS` | FCM push (Android 13+) |
| `VIBRATE` | FCM notification vibration |

---

## ✅ Android 14/15 Compliance

- Targets SDK 35 (Android 15)
- `PendingIntent.FLAG_IMMUTABLE` set on all PendingIntents
- `POST_NOTIFICATIONS` permission declared for Android 13+
- `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` replacing legacy storage permissions
- `data_extraction_rules.xml` included for Android 12+
- All Activities marked `exported` correctly

---

## 🛠 Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android Gradle Plugin 8.7+
- A Firebase project (free tier is fine)
- An AdMob account (optional)
