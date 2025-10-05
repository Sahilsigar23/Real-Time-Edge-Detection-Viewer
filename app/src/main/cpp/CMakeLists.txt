cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

# Set C++ standard
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV configuration
# NOTE: Update this path to your OpenCV Android SDK location
# Download from: https://opencv.org/releases/
set(OpenCV_DIR "C:/opencv-android-sdk/sdk/native/jni")

# Try to find OpenCV
find_package(OpenCV REQUIRED)

if(OpenCV_FOUND)
    message(STATUS "OpenCV found: ${OpenCV_VERSION}")
    message(STATUS "OpenCV include dirs: ${OpenCV_INCLUDE_DIRS}")
    message(STATUS "OpenCV libs: ${OpenCV_LIBS}")
else()
    message(FATAL_ERROR "OpenCV not found. Please set OpenCV_DIR correctly.")
endif()

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Add native library
add_library(native-lib SHARED
        native-lib.cpp)

# Link libraries
target_link_libraries(native-lib
        ${OpenCV_LIBS}
        android
        log
        jnigraphics)
