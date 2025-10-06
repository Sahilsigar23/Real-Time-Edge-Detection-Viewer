cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

# Set C++ standard
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV configuration
# NOTE: Update this path to your OpenCV Android SDK location
# Download from: https://opencv.org/releases/
# Example paths:
#   Windows: C:/opencv-android-sdk/sdk/native/jni
#   Linux/Mac: /path/to/opencv-android-sdk/sdk/native/jni
set(OpenCV_DIR "OPENCV_SDK_PATH_HERE" CACHE PATH "Path to OpenCV Android SDK")

# Try to find OpenCV - make it optional for initial setup
if(EXISTS "${OpenCV_DIR}")
    find_package(OpenCV)
    if(OpenCV_FOUND)
        message(STATUS "✓ OpenCV found: ${OpenCV_VERSION}")
        message(STATUS "✓ OpenCV include: ${OpenCV_INCLUDE_DIRS}")
        message(STATUS "✓ OpenCV libs: ${OpenCV_LIBS}")
        set(OPENCV_ENABLED ON)
    else()
        message(WARNING "OpenCV directory exists but package not found")
        set(OPENCV_ENABLED OFF)
    endif()
else()
    message(WARNING "========================================")
    message(WARNING "OpenCV Android SDK not found!")
    message(WARNING "Please:")
    message(WARNING "1. Download from: https://opencv.org/releases/")
    message(WARNING "2. Extract the SDK")
    message(WARNING "3. Update OpenCV_DIR in CMakeLists.txt")
    message(WARNING "4. Re-sync Gradle")
    message(WARNING "========================================")
    set(OPENCV_ENABLED OFF)
endif()

# Add native library
add_library(native-lib SHARED native-lib.cpp)

# Link libraries
if(OPENCV_ENABLED)
    target_include_directories(native-lib PRIVATE ${OpenCV_INCLUDE_DIRS})
    target_link_libraries(native-lib
            ${OpenCV_LIBS}
            android
            log
            jnigraphics)
    target_compile_definitions(native-lib PRIVATE OPENCV_ENABLED)
    message(STATUS "✓ Building with OpenCV support")
else()
    target_link_libraries(native-lib
            android
            log
            jnigraphics)
    message(STATUS "⚠ Building WITHOUT OpenCV (stub mode)")
endif()
