# 🚀 Setup and Run Guide

Complete guide to set up and run the Edge Detector project.

## Prerequisites

### For Android App
- ✅ **Android Studio** Arctic Fox or later
- ✅ **Android SDK** API 24 (Android 7.0) or higher
- ✅ **Android NDK** r21 or later
- ✅ **OpenCV for Android** 4.x
- ✅ **Physical Android device** with camera (recommended) or emulator

### For Web Viewer
- ✅ **Node.js** 14+ and npm
- ✅ **TypeScript** 5.3+ (will be installed via npm)
- ✅ **Modern web browser** (Chrome, Firefox, Safari, Edge)

---

## 🔧 Part 1: Android App Setup

### Step 1: Install Android Studio

1. Download from https://developer.android.com/studio
2. Install with default settings
3. Open Android Studio and complete the setup wizard

### Step 2: Install Android NDK

1. Open Android Studio
2. Go to **Tools → SDK Manager**
3. Click **SDK Tools** tab
4. Check **NDK (Side by side)**
5. Click **Apply** to install

### Step 3: Download OpenCV Android SDK

1. Visit https://opencv.org/releases/
2. Download **OpenCV Android** (e.g., `opencv-4.8.0-android-sdk.zip`)
3. Extract to a location (e.g., `C:\opencv-android-sdk\`)
4. Note the path for later

### Step 4: Configure OpenCV Path

1. Open `app/src/main/cpp/CMakeLists.txt`
2. Update line 11 with your OpenCV path:
   ```cmake
   set(OpenCV_DIR "C:/opencv-android-sdk/sdk/native/jni")
   ```
   Replace with your actual OpenCV path

### Step 5: Open Project in Android Studio

1. Launch Android Studio
2. Click **File → Open**
3. Navigate to the project folder (`Flam-assignment`)
4. Click **OK**
5. Wait for Gradle sync to complete (may take a few minutes)

### Step 6: Resolve Any Issues

If you see errors:

**Gradle Sync Failed:**
- Go to **File → Invalidate Caches → Invalidate and Restart**

**NDK Not Found:**
- Go to **File → Project Structure → SDK Location**
- Set NDK location (usually auto-detected)

**OpenCV Not Found:**
- Double-check the path in `CMakeLists.txt`
- Ensure you downloaded the Android SDK (not desktop version)

### Step 7: Connect Android Device

**Option A: Physical Device (Recommended)**
1. Enable **Developer Options** on your Android device:
   - Go to **Settings → About Phone**
   - Tap **Build Number** 7 times
2. Enable **USB Debugging**:
   - Go to **Settings → Developer Options**
   - Turn on **USB Debugging**
3. Connect device via USB
4. Accept the debugging prompt on your device

**Option B: Emulator**
1. Go to **Tools → Device Manager**
2. Click **Create Device**
3. Select a device (e.g., Pixel 5)
4. Select system image (API 28+ recommended)
5. Click **Finish**

### Step 8: Build and Run

1. Select your device from the device dropdown
2. Click the **Run** button (green play icon) or press `Shift + F10`
3. Wait for the app to build and install
4. Grant camera permission when prompted

### Step 9: Use the App

1. Point camera at objects with clear edges
2. Click **Toggle Processing** to enable edge detection
3. View FPS counter in top-left
4. Processing time is logged to console

**Troubleshooting:**
- **Black screen:** Check camera permissions
- **App crashes:** Check Logcat for errors (`View → Tool Windows → Logcat`)
- **Build fails:** Check NDK and OpenCV paths

---

## 🌐 Part 2: Web Viewer Setup

### Step 1: Install Node.js

1. Download from https://nodejs.org/
2. Install the LTS version
3. Verify installation:
   ```bash
   node --version
   npm --version
   ```

### Step 2: Install Dependencies

Open terminal/command prompt and navigate to the web directory:

```bash
cd web
npm install
```

This installs:
- TypeScript compiler
- Type definitions
- Express (for optional server)

### Step 3: Build TypeScript

Compile TypeScript to JavaScript:

```bash
npm run build
```

This creates the `dist/` folder with compiled JavaScript.

**For development with auto-rebuild:**
```bash
npm run watch
```

### Step 4: Run the Web Viewer

**Option A: Open HTML File Directly**
1. Navigate to `web/public/`
2. Double-click `index.html`
3. It opens in your default browser

**Option B: Use Python HTTP Server**
```bash
cd web/public
python -m http.server 8080
```
Then open: http://localhost:8080

**Option C: Use Node.js http-server**
```bash
# Install globally (one time)
npm install -g http-server

# Run from web/public directory
cd web/public
http-server -p 8080
```
Then open: http://localhost:8080

### Step 5: View the Demo

The web page will display:
- Sample edge-detected frame (placeholder)
- Frame statistics (FPS, resolution, processing time)
- Refresh button to simulate new frames

**Note:** In production, this would connect to the Android app via WebSocket or REST API to receive real frames.

---

## 🧪 Part 3: Testing the Complete System

### Test Android App

1. **Camera Feed Test:**
   - Launch app
   - Verify camera preview appears
   - Check FPS counter updates

2. **Edge Detection Test:**
   - Click "Toggle Processing"
   - Point camera at objects with clear edges (books, doors, windows)
   - Edges should appear as white lines on black background

3. **Performance Test:**
   - Monitor FPS (should be 10-30 FPS depending on device)
   - Check Logcat for processing times
   - No app crashes or freezes

### Test Web Viewer

1. **Page Load Test:**
   - Open web page
   - Verify sample image loads
   - Check statistics display

2. **Interaction Test:**
   - Click "Refresh Frame" button
   - Statistics should update with new random values

3. **Console Test:**
   - Open browser Developer Tools (F12)
   - Check Console for initialization messages
   - Verify no errors

---

## 📱 Building APK for Distribution

### Debug APK (Testing)

```bash
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (Production)

1. Generate keystore:
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```

2. Add to `app/build.gradle`:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file('my-release-key.jks')
               storePassword 'your-password'
               keyAlias 'my-key-alias'
               keyPassword 'your-password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
               minifyEnabled true
               proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
           }
       }
   }
   ```

3. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

APK location: `app/build/outputs/apk/release/app-release.apk`

---

## 🐛 Common Issues and Solutions

### Android Issues

| Issue | Solution |
|-------|----------|
| "OpenCV not found" | Update OpenCV path in CMakeLists.txt |
| "NDK not configured" | Install NDK via SDK Manager |
| App crashes on start | Check camera permissions in Settings |
| Black screen | Grant camera permission when prompted |
| Build fails | Clean project: `Build → Clean Project` |
| Gradle sync fails | Invalidate caches and restart Android Studio |

### Web Viewer Issues

| Issue | Solution |
|-------|----------|
| TypeScript errors | Run `npm install` to get dependencies |
| Compile fails | Check tsconfig.json and file paths |
| Page not loading | Ensure dist/ folder exists (run `npm run build`) |
| Image not showing | Check browser console for errors |
| CORS errors | Use HTTP server, don't open file:// directly |

---

## 📊 Performance Optimization

### Android App
- Lower camera resolution in `CameraHandler.java` (line 28-29)
- Reduce Canny threshold values in `native-lib.cpp`
- Disable debug logging in release builds

### Web Viewer
- Compress images before sending from Android
- Use WebSocket for real-time streaming
- Implement frame buffering

---

## 🎯 Next Steps

1. ✅ Run Android app and verify edge detection works
2. ✅ Open web viewer and check display
3. 🔄 Implement WebSocket connection between app and web
4. 🔄 Add more OpenCV filters (blur, threshold, etc.)
5. 🔄 Deploy web viewer to cloud (Heroku, Vercel, etc.)

---

## 📞 Support

For issues:
1. Check Logcat for Android errors
2. Check browser Console for web errors
3. Review README.md for architecture details
4. Check CMakeLists.txt for OpenCV configuration

---

**Project Status:** ✅ Ready to Run
**Last Updated:** October 2025
**Assessment:** Flam RnD Intern
