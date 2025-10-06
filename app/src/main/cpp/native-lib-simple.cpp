#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstring>

#define LOG_TAG "NativeProcessor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

/**
 * Simple grayscale conversion without OpenCV for testing
 */
JNIEXPORT jbyteArray JNICALL
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

    jsize dataLength = env->GetArrayLength(frameData);
    jbyte *frameBytes = env->GetByteArrayElements(frameData, nullptr);
    
    if (frameBytes == nullptr) {
        LOGE("Failed to get frame bytes");
        return nullptr;
    }

    // Create output array (same size as input)
    jbyteArray outputArray = env->NewByteArray(dataLength);
    if (outputArray == nullptr) {
        env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);
        return nullptr;
    }

    // Simple grayscale conversion (since OpenCV might not be available)
    unsigned char* output = new unsigned char[dataLength];
    
    for (int i = 0; i < dataLength; i += 4) {
        unsigned char r = frameBytes[i];
        unsigned char g = frameBytes[i + 1];
        unsigned char b = frameBytes[i + 2];
        
        // Grayscale conversion
        unsigned char gray = (unsigned char)(0.299f * r + 0.587f * g + 0.114f * b);
        
        // Simple edge detection approximation: Invert grayscale
        unsigned char edge = 255 - gray;
        
        output[i] = edge;
        output[i + 1] = edge;
        output[i + 2] = edge;
        output[i + 3] = 255; // Alpha
    }

    env->SetByteArrayRegion(outputArray, 0, dataLength, (jbyte*)output);
    
    delete[] output;
    env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);
    
    LOGD("Frame processed (simple mode)");
    return outputArray;
}

/**
 * Convert to grayscale
 */
JNIEXPORT jbyteArray JNICALL
Java_com_flam_edgedetector_NativeProcessor_toGrayscale(
        JNIEnv *env,
        jclass clazz,
        jbyteArray frameData,
        jint width,
        jint height) {

    if (frameData == nullptr) {
        return nullptr;
    }

    jsize dataLength = env->GetArrayLength(frameData);
    jbyte *frameBytes = env->GetByteArrayElements(frameData, nullptr);
    
    if (frameBytes == nullptr) {
        return nullptr;
    }

    jbyteArray outputArray = env->NewByteArray(dataLength);
    unsigned char* output = new unsigned char[dataLength];
    
    for (int i = 0; i < dataLength; i += 4) {
        unsigned char r = frameBytes[i];
        unsigned char g = frameBytes[i + 1];
        unsigned char b = frameBytes[i + 2];
        unsigned char gray = (unsigned char)(0.299f * r + 0.587f * g + 0.114f * b);
        
        output[i] = gray;
        output[i + 1] = gray;
        output[i + 2] = gray;
        output[i + 3] = 255;
    }

    env->SetByteArrayRegion(outputArray, 0, dataLength, (jbyte*)output);
    delete[] output;
    env->ReleaseByteArrayElements(frameData, frameBytes, JNI_ABORT);
    
    return outputArray;
}

/**
 * Get version info
 */
JNIEXPORT jstring JNICALL
Java_com_flam_edgedetector_NativeProcessor_getOpenCVVersion(
        JNIEnv *env,
        jclass clazz) {
    return env->NewStringUTF("Native Library v1.0 (Simple Mode)");
}

} // extern "C"
