package com.flam.edgedetector;

import android.util.Log;

/**
 * JNI Bridge class for processing frames using native C++ and OpenCV
 */
public class NativeProcessor {
    private static final String TAG = "NativeProcessor";

    private static boolean librariesLoaded = false;
    private static String loadError = null;

    static {
        try {
            // Load OpenCV library first (required dependency)
            System.loadLibrary("opencv_java4");
            Log.d(TAG, "OpenCV library loaded successfully");
            
            // Then load our native library
            System.loadLibrary("native-lib");
            Log.d(TAG, "Native library loaded successfully");
            librariesLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            loadError = e.getMessage();
            Log.e(TAG, "Failed to load native libraries: " + e.getMessage());
            Log.e(TAG, "Stack trace: ", e);
        }
    }

    /**
     * Check if native libraries are loaded
     */
    public static boolean isLoaded() {
        return librariesLoaded;
    }

    /**
     * Get the load error message if libraries failed to load
     */
    public static String getLoadError() {
        return loadError;
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
