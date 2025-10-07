package com.flam.edgedetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Camera handler using Camera2 API for capturing frames
 */
public class CameraHandler {
    private static final String TAG = "CameraHandler";
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 480;

    private final Context context;
    private final FrameCallback frameCallback;
    
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    public interface FrameCallback {
        void onFrameAvailable(byte[] frameData, int width, int height);
    }

    public CameraHandler(Context context, FrameCallback callback) {
        this.context = context;
        this.frameCallback = callback;
    }

    public void startCamera() {
        startBackgroundThread();
        openCamera();
    }

    public void stopCamera() {
        closeCamera();
        stopBackgroundThread();
    }

    public void release() {
        stopCamera();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping background thread", e);
            }
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            Log.e(TAG, "Camera manager is null");
            return;
        }
        
        try {
            String cameraId = getCameraId(manager);
            if (cameraId == null) {
                Log.e(TAG, "No suitable camera found");
                return;
            }

            // Set up ImageReader
            imageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT, 
                    ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        byte[] rgbaData = convertYUVtoRGBA(image);
                        if (frameCallback != null && rgbaData != null) {
                            frameCallback.onFrameAvailable(rgbaData, image.getWidth(), image.getHeight());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing image", e);
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            }, backgroundHandler);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Camera permission not granted");
                return;
            }

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    Log.e(TAG, "Camera error: " + error);
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
        }
    }

    private String getCameraId(CameraManager manager) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error getting camera ID", e);
        }
        return null;
    }

    private void createCaptureSession() {
        if (cameraDevice == null) {
            Log.e(TAG, "Cannot create capture session - camera device is null");
            return;
        }
        
        if (imageReader == null) {
            Log.e(TAG, "Cannot create capture session - image reader is null");
            return;
        }
        
        try {
            Log.d(TAG, "Creating capture session...");
            cameraDevice.createCaptureSession(
                    Arrays.asList(imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) {
                                Log.w(TAG, "Camera was closed before session configured");
                                return;
                            }
                            Log.d(TAG, "Capture session configured successfully");
                            captureSession = session;
                            
                            // Add a small delay to ensure everything is ready
                            backgroundHandler.postDelayed(() -> startCapture(), 100);
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Capture session configuration failed");
                        }
                    },
                    backgroundHandler
            );
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error creating capture session", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid argument creating capture session", e);
        }
    }

    private void startCapture() {
        if (cameraDevice == null) {
            Log.e(TAG, "Cannot start capture - camera device is null");
            return;
        }
        
        if (captureSession == null) {
            Log.e(TAG, "Cannot start capture - capture session is null");
            return;
        }
        
        if (imageReader == null) {
            Log.e(TAG, "Cannot start capture - image reader is null");
            return;
        }
        
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW);
            
            // Verify the surface is valid before adding it
            if (imageReader.getSurface() == null) {
                Log.e(TAG, "ImageReader surface is null");
                return;
            }
            
            if (!imageReader.getSurface().isValid()) {
                Log.e(TAG, "ImageReader surface is not valid");
                return;
            }
            
            builder.addTarget(imageReader.getSurface());
            builder.set(CaptureRequest.CONTROL_AF_MODE, 
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            
            CaptureRequest request = builder.build();
            captureSession.setRepeatingRequest(request, null, backgroundHandler);
            Log.d(TAG, "Capture started successfully");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error starting capture", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Capture session in invalid state", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid surface configuration", e);
        }
    }

    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    /**
     * Convert YUV_420_888 image to RGBA byte array
     */
    private byte[] convertYUVtoRGBA(Image image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];
            
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            byte[] rgbaData = new byte[width * height * 4];
            convertYUV420ToRGBA8888(nv21, rgbaData, width, height);
            
            return rgbaData;
        } catch (Exception e) {
            Log.e(TAG, "Error converting YUV to RGBA", e);
            return null;
        }
    }

    /**
     * YUV420 to RGBA conversion
     */
    private void convertYUV420ToRGBA8888(byte[] yuv420, byte[] rgba, int width, int height) {
        final int frameSize = width * height;
        
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int y = (0xff & yuv420[j * width + i]);
                int v = (0xff & yuv420[frameSize + (j >> 1) * width + (i & ~1)]);
                int u = (0xff & yuv420[frameSize + (j >> 1) * width + (i & ~1) + 1]);
                
                y = y < 16 ? 16 : y;
                
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                
                int index = (j * width + i) * 4;
                rgba[index] = (byte) r;
                rgba[index + 1] = (byte) g;
                rgba[index + 2] = (byte) b;
                rgba[index + 3] = (byte) 255; // Alpha
            }
        }
    }
}
