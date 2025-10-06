#include <jni.h>
#include <string>
#include <android/log.h>

#ifdef OPENCV_ENABLED
#include <opencv2/opencv.hpp>
#endif

#define LOG_TAG "NativeProcessor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

/**
 * Process frame using OpenCV Canny edge detection
 */
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_flam_edgedetector_NativeProcessor_processFrame(
        JNIEnv *env,
        jclass clazz,
        jbyteArray frameData,
        jint width,
        jint height) {

    if (frameData == nullptr) {
        LOGE("Frame data is null");
        return nullptr;
    }

#ifdef OPENCV_ENABLED
    try {
        // Get frame data from Java
        jbyte *frameBytes = env->GetByteArrayElements(frameData, nullptr);
        if (frameBytes == nullptr) {
            LOGE("Failed to get frame bytes");
            return nullptr;
        }

        jsize dataLength = env->GetArrayLength(frameData);
        LOGD("Processing frame: %dx%d, data length: %d", width, height, dataLength);

        // Create OpenCV Mat from RGBA data
        cv::Mat rgbaMat(height, width, CV_8UC4, (unsigned char *)frameBytes);

        // Convert to grayscale
        cv::Mat grayMat;
        cv::cvtColor(rgbaMat, grayMat, cv::COLOR_RGBA2GRAY);

        // Apply Gaussian blur to reduce noise
        cv::Mat blurredMat;
        cv::GaussianBlur(grayMat, blurredMat, cv::Size(5, 5), 1.5);

        // Apply Canny edge detection
        cv::Mat edgesMat;
        cv::Canny(blurredMat, edgesMat, 50, 150);

        // Convert edges back to RGBA (edges will be white on black background)
        cv::Mat outputMat;
        cv::cvtColor(edgesMat, outputMat, cv::COLOR_GRAY2RGBA);

        // Create output byte array
        jsize outputLength = outputMat.total() * outputMat.elemSize();
        jbyteArray outputArray = env->NewByteArray(outputLength);
        if (outputArray == nullptr) {
            LOGE("Failed to create output array");
            env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);
            return nullptr;
        }

        // Copy processed data to output array
        env->SetByteArrayRegion(outputArray, 0, outputLength, 
                                (jbyte *)outputMat.data);

        // Release input array
        env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);

        LOGD("Frame processed successfully");
        return outputArray;

    } catch (cv::Exception &e) {
        LOGE("OpenCV exception: %s", e.what());
        return nullptr;
    } catch (...) {
        LOGE("Unknown exception during frame processing");
        return nullptr;
    }
#else
    LOGE("OpenCV not configured - returning original frame");
    // Return the original frame unchanged
    jsize dataLength = env->GetArrayLength(frameData);
    jbyteArray outputArray = env->NewByteArray(dataLength);
    if (outputArray != nullptr) {
        jbyte *frameBytes = env->GetByteArrayElements(frameData, nullptr);
        if (frameBytes != nullptr) {
            env->SetByteArrayRegion(outputArray, 0, dataLength, frameBytes);
            env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);
        }
    }
    return outputArray;
#endif
}

/**
 * Convert frame to grayscale
 */
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_flam_edgedetector_NativeProcessor_toGrayscale(
        JNIEnv *env,
        jclass clazz,
        jbyteArray frameData,
        jint width,
        jint height) {

    if (frameData == nullptr) {
        LOGE("Frame data is null");
        return nullptr;
    }

#ifdef OPENCV_ENABLED
    try {
        // Get frame data from Java
        jbyte *frameBytes = env->GetByteArrayElements(frameData, nullptr);
        if (frameBytes == nullptr) {
            LOGE("Failed to get frame bytes");
            return nullptr;
        }

        // Create OpenCV Mat from RGBA data
        cv::Mat rgbaMat(height, width, CV_8UC4, (unsigned char *)frameBytes);

        // Convert to grayscale
        cv::Mat grayMat;
        cv::cvtColor(rgbaMat, grayMat, cv::COLOR_RGBA2GRAY);

        // Convert back to RGBA
        cv::Mat outputMat;
        cv::cvtColor(grayMat, outputMat, cv::COLOR_GRAY2RGBA);

        // Create output byte array
        jsize outputLength = outputMat.total() * outputMat.elemSize();
        jbyteArray outputArray = env->NewByteArray(outputLength);
        if (outputArray == nullptr) {
            LOGE("Failed to create output array");
            env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);
            return nullptr;
        }

        // Copy processed data to output array
        env->SetByteArrayRegion(outputArray, 0, outputLength, 
                                (jbyte *)outputMat.data);

        // Release input array
        env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);

        return outputArray;

    } catch (cv::Exception &e) {
        LOGE("OpenCV exception: %s", e.what());
        return nullptr;
    } catch (...) {
        LOGE("Unknown exception during grayscale conversion");
        return nullptr;
    }
#else
    LOGE("OpenCV not configured - returning original frame");
    jsize dataLength = env->GetArrayLength(frameData);
    jbyteArray outputArray = env->NewByteArray(dataLength);
    if (outputArray != nullptr) {
        jbyte *frameBytes = env->GetByteArrayElements(frameData, nullptr);
        if (frameBytes != nullptr) {
            env->SetByteArrayRegion(outputArray, 0, dataLength, frameBytes);
            env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);
        }
    }
    return outputArray;
#endif
}

/**
 * Get OpenCV version
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_flam_edgedetector_NativeProcessor_getOpenCVVersion(
        JNIEnv *env,
        jclass clazz) {
    
#ifdef OPENCV_ENABLED
    std::string version = cv::getVersionString();
    LOGD("OpenCV Version: %s", version.c_str());
    return env->NewStringUTF(version.c_str());
#else
    LOGD("OpenCV not configured");
    return env->NewStringUTF("OpenCV Not Configured - Please download and configure OpenCV Android SDK");
#endif
}

} // extern "C"
