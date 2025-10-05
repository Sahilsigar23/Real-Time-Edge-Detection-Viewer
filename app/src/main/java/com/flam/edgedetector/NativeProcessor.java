package com.flam.edgedetector;

import android.util.Log;

/**
 * JNI Bridge class for processing frames using native C++ and OpenCV
 */
public class NativeProcessor {
    private static final String TAG = "NativeProcessor";

    static {
        try {
            System.loadLibrary("native-lib");
            Log.d(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library: " + e.getMessage());
        }
    }

    /**
     * Process a frame using OpenCV Canny edge detection in native C++
     *
     * @param frameData Input frame data in RGBA format
     * @param width Frame width
     * @param height Frame height
     * @return Processed frame data (edge detected)
     */
    public static native byte[] processFrame(byte[] frameData, int width, int height);

    /**
     * Apply grayscale conversion to a frame
     *
     * @param frameData Input frame data in RGBA format
     * @param width Frame width
     * @param height Frame height
     * @return Grayscale frame data
     */
    public static native byte[] toGrayscale(byte[] frameData, int width, int height);

    /**
     * Get OpenCV version info
     *
     * @return OpenCV version string
     */
    public static native String getOpenCVVersion();
}
