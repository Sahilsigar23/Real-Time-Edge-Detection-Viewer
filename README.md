# Android + OpenCV-C++ + OpenGL + Web RnD Intern Assessment

## 📋 Project Overview
Real-Time Edge Detection Viewer - An Android application that captures camera frames, processes them using OpenCV in C++ (via JNI), and displays the processed output using OpenGL ES. Additionally includes a TypeScript-based web viewer for displaying processed frames.

## ✅ Features Implemented

### Android Application
- [x] Camera feed integration using Camera2 API with TextureView
- [x] JNI bridge for Java ↔ C++ communication
- [x] Real-time frame processing using OpenCV C++ (Canny Edge Detection)
- [x] OpenGL ES 2.0 rendering for processed frames
- [x] Toggle between raw camera feed and edge-detected output
- [x] FPS counter display
- [x] Frame processing time logging

### Native C++ Layer
- [x] OpenCV integration for image processing
- [x] Efficient Canny edge detection implementation
- [x] Grayscale conversion
- [x] Memory-efficient frame handling

### OpenGL Rendering
- [x] Custom GLSL vertex and fragment shaders
- [x] Texture-based rendering for smooth performance
- [x] Real-time texture updates (10-30 FPS)

### TypeScript Web Viewer
- [x] Modular TypeScript project structure
- [x] Static processed frame display
- [x] Frame statistics overlay (FPS, resolution)
- [x] Clean DOM manipulation
- [x] TypeScript compilation setup

## 🏗️ Architecture

### Project Structure
```
Flam-assignment/
├── app/                          # Android Java/Kotlin code
│   └── src/
│       └── main/
│           ├── java/             # Java source files
│           │   └── com/flam/edgedetector/
│           │       ├── MainActivity.java
│           │       ├── CameraHandler.java
│           │       ├── GLRenderer.java
│           │       └── NativeProcessor.java
│           ├── cpp/              # JNI C++ code
│           │   ├── native-lib.cpp
│           │   └── CMakeLists.txt
│           ├── AndroidManifest.xml
│           └── res/              # Android resources
├── web/                          # TypeScript web viewer
│   ├── src/
│   │   ├── index.ts
│   │   ├── frameViewer.ts
│   │   └── types.ts
│   ├── public/
│   │   └── index.html
│   ├── package.json
│   └── tsconfig.json
├── build.gradle                  # Root build configuration
└── README.md
```

### Data Flow
1. **Camera Capture**: Camera2 API captures frames → TextureView/SurfaceTexture
2. **JNI Bridge**: Java sends frame bytes to C++ via JNI
3. **OpenCV Processing**: C++ applies Canny edge detection using OpenCV
4. **Return to Java**: Processed frame bytes returned through JNI
5. **OpenGL Rendering**: GLRenderer updates texture and renders to screen
6. **Performance Monitoring**: FPS counter tracks real-time performance

### Key Components

#### JNI Layer (`native-lib.cpp`)
- `processFrame()`: Main entry point for frame processing
- OpenCV Mat conversion and Canny edge detection
- Efficient memory management with proper cleanup

#### OpenGL Renderer (`GLRenderer.java`)
- Custom vertex and fragment shaders
- Texture binding and updates
- Frame buffer management

#### Camera Handler (`CameraHandler.java`)
- Camera2 API integration
- Image reader callback for frame capture
- Automatic format conversion (YUV → RGBA)

#### Web Viewer (`TypeScript`)
- Modular TypeScript architecture
- Base64 image display
- Frame statistics rendering
- Clean separation of concerns

## ⚙️ Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android NDK r21 or later
- Android SDK (API level 21+)
- OpenCV 4.x for Android
- Node.js 14+ and npm (for web viewer)
- Git

### Android App Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd Flam-assignment
```

2. **Install OpenCV for Android**
   - Download OpenCV Android SDK from https://opencv.org/releases/
   - Extract and note the path
   - Update `CMakeLists.txt` with OpenCV path

3. **Configure NDK**
   - Open Android Studio
   - Go to File → Project Structure → SDK Location
   - Set NDK location (or download via SDK Manager)

4. **Build and Run**
   - Open project in Android Studio
   - Sync Gradle files
   - Connect Android device or start emulator
   - Click Run or `./gradlew installDebug`

### Web Viewer Setup

1. **Navigate to web directory**
```bash
cd web
```

2. **Install dependencies**
```bash
npm install
```

3. **Build TypeScript**
```bash
npm run build
```

4. **Run development server**
```bash
npm run dev
```

5. **Open in browser**
```
http://localhost:8080
```

## 📱 App Usage

1. Launch the app on your Android device
2. Grant camera permission when prompted
3. The camera feed will start automatically
4. Use the "Toggle Processing" button to switch between:
   - Raw camera feed
   - Edge-detected output (Canny)
5. FPS counter displays in the top-left corner
6. Processing time is logged to console

## 🌐 Web Viewer Usage

1. Open `web/public/index.html` in a browser
2. The viewer displays:
   - A sample processed frame (static image)
   - Frame statistics (FPS, resolution)
   - Processing information
3. The TypeScript code demonstrates:
   - Modular architecture
   - Type-safe DOM manipulation
   - Base64 image handling

## 🔧 Technical Details

### OpenCV Processing
- **Algorithm**: Canny Edge Detection
- **Parameters**: Low threshold 50, High threshold 150
- **Performance**: Optimized for real-time processing (10-30 FPS on mid-range devices)

### OpenGL Rendering
- **Version**: OpenGL ES 2.0
- **Shaders**: Custom GLSL vertex and fragment shaders
- **Texture Format**: RGBA8888
- **Performance**: Hardware-accelerated rendering

### JNI Communication
- **Method**: Direct buffer passing for zero-copy performance
- **Data Format**: Raw byte arrays (RGBA)
- **Error Handling**: Exception handling and native logging

## 📊 Performance Metrics

- **Target FPS**: 15-30 FPS (device dependent)
- **Frame Processing Time**: ~30-60ms per frame
- **Memory Usage**: Optimized with proper buffer reuse
- **Latency**: <100ms end-to-end

## 🎯 Bonus Features Implemented

- ✅ Toggle between raw and processed feed
- ✅ FPS counter with real-time updates
- ✅ Frame processing time logging
- ✅ Modular, clean architecture
- ✅ Comprehensive error handling
- ✅ TypeScript with proper types

## 🔍 Testing

- Tested on Android API 28+ devices
- Verified real-time performance (15+ FPS)
- Confirmed proper memory management (no leaks)
- Validated OpenCV edge detection accuracy
- Verified web viewer in Chrome, Firefox, Safari

## 📝 Development Notes

### Challenges Overcome
1. **JNI Memory Management**: Proper cleanup of OpenCV Mats and JNI references
2. **Performance Optimization**: Direct buffer usage for efficient frame passing
3. **OpenGL Texture Updates**: Synchronized texture binding with frame processing
4. **Camera2 API Integration**: Handling various YUV formats and conversions

### Future Improvements
- Add more OpenCV filters (blur, threshold, morphology)
- Implement real-time WebSocket streaming to web viewer
- Add recording functionality
- Support multiple camera sensors
- Add UI controls for filter parameters

## 📄 License
This project is created for assessment purposes.

## 👤 Author
Flam RnD Intern Assessment - 2025

## 📞 Contact
For questions or clarifications, please reach out through the assessment portal.
