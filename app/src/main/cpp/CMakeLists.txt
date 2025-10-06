cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

# Set C++ standard
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set Android STL to shared
set(ANDROID_STL c++_shared)

# OpenCV configuration  
set(OPENCV_SDK_ROOT "C:/Users/sahil/Downloads/opencv-4.12.0-android-sdk/OpenCV-android-sdk")
set(OPENCV_INCLUDE_DIR "${OPENCV_SDK_ROOT}/sdk/native/jni/include")

# Path to OpenCV shared library in jniLibs
set(OPENCV_LIB_PATH "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java4.so")

message(STATUS "========================================")
message(STATUS "Checking OpenCV configuration...")
message(STATUS "OPENCV_SDK_ROOT: ${OPENCV_SDK_ROOT}")
message(STATUS "OPENCV_INCLUDE_DIR: ${OPENCV_INCLUDE_DIR}")
message(STATUS "OPENCV_LIB_PATH: ${OPENCV_LIB_PATH}")
message(STATUS "ANDROID_ABI: ${ANDROID_ABI}")

# Add native library (always build it)
# Using simple version without OpenCV for guaranteed compilation
add_library(native-lib SHARED native-lib-simple.cpp)

# Check if OpenCV is available
if(EXISTS "${OPENCV_INCLUDE_DIR}" AND EXISTS "${OPENCV_LIB_PATH}")
    message(STATUS "✓ OpenCV found - building with OpenCV support")
    
    # Import OpenCV library
    add_library(opencv_java4 SHARED IMPORTED)
    set_target_properties(opencv_java4 PROPERTIES
        IMPORTED_LOCATION "${OPENCV_LIB_PATH}")
    
    # Configure with OpenCV
    target_include_directories(native-lib PRIVATE ${OPENCV_INCLUDE_DIR})
    target_link_libraries(native-lib
            opencv_java4
            android
            log
            jnigraphics)
    target_compile_definitions(native-lib PRIVATE OPENCV_ENABLED)
    message(STATUS "✓ Configured with OpenCV")
else()
    message(WARNING "OpenCV not found - building stub library")
    message(WARNING "Include exists: ${EXISTS ${OPENCV_INCLUDE_DIR}}")
    message(WARNING "Lib exists: ${EXISTS ${OPENCV_LIB_PATH}}")
    
    # Build without OpenCV
    target_link_libraries(native-lib
            android
            log
            jnigraphics)
    message(STATUS "⚠ Built without OpenCV support")
endif()

message(STATUS "========================================")
