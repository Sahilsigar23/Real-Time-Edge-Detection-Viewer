cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

# Set C++ standard
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set Android STL to shared
set(ANDROID_STL c++_shared)

# OpenCV configuration  
set(OPENCV_SDK_ROOT "C:/Users/sahil/Downloads/opencv-4.12.0-android-sdk/OpenCV-android-sdk")

# Check if OpenCV exists
if(EXISTS "${OPENCV_SDK_ROOT}")
    message(STATUS "✓ OpenCV SDK found at: ${OPENCV_SDK_ROOT}")
    
    # Set paths for OpenCV
    set(OPENCV_INCLUDE_DIR "${OPENCV_SDK_ROOT}/sdk/native/jni/include")
    set(OPENCV_LIB_DIR "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}")
    
    # Check if include directory and jniLibs exist
    if(EXISTS "${OPENCV_INCLUDE_DIR}" AND EXISTS "${OPENCV_LIB_DIR}/libopencv_java4.so")
        message(STATUS "✓ OpenCV include dir: ${OPENCV_INCLUDE_DIR}")
        message(STATUS "✓ OpenCV lib dir: ${OPENCV_LIB_DIR}")
        
        # Import OpenCV Java wrapper (all-in-one shared library for Android)
        add_library(opencv_java4 SHARED IMPORTED)
        set_target_properties(opencv_java4 PROPERTIES
            IMPORTED_LOCATION "${OPENCV_LIB_DIR}/libopencv_java4.so")
        
        set(OPENCV_ENABLED ON)
        message(STATUS "✓ OpenCV shared library imported successfully")
    else()
        message(WARNING "OpenCV libs not found. Run copy_opencv_libs.bat first!")
        set(OPENCV_ENABLED OFF)
    endif()
else()
    message(WARNING "========================================")
    message(WARNING "OpenCV Android SDK not found!")
    message(WARNING "1. Download from: https://opencv.org/releases/")
    message(WARNING "2. Update OPENCV_SDK_ROOT in CMakeLists.txt")
    message(WARNING "3. Run copy_opencv_libs.bat")
    message(WARNING "========================================")
    set(OPENCV_ENABLED OFF)
endif()

# Add native library
add_library(native-lib SHARED native-lib.cpp)

# Configure native-lib
if(OPENCV_ENABLED)
    target_include_directories(native-lib PRIVATE ${OPENCV_INCLUDE_DIR})
    target_link_libraries(native-lib
            opencv_java4
            android
            log
            jnigraphics)
    target_compile_definitions(native-lib PRIVATE OPENCV_ENABLED)
    message(STATUS "✓ Building with OpenCV (opencv_java4)")
else()
    target_link_libraries(native-lib
            android
            log
            jnigraphics)
    message(STATUS "⚠ Building WITHOUT OpenCV (stub mode)")
endif()
