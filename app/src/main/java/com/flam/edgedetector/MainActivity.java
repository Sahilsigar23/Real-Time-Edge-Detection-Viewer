package com.flam.edgedetector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;
    private CameraHandler cameraHandler;
    private TextView fpsTextView;
    private TextView statusTextView;
    private MaterialButton toggleButton;

    private boolean isProcessingEnabled = false;
    private long lastFrameTime = 0;
    private float currentFps = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        glSurfaceView = findViewById(R.id.glSurfaceView);
        fpsTextView = findViewById(R.id.fpsTextView);
        statusTextView = findViewById(R.id.statusTextView);
        toggleButton = findViewById(R.id.toggleButton);

        // Check if native libraries are loaded
        if (!NativeProcessor.isLoaded()) {
            Log.w(TAG, "Native library not available - using Java fallback");
            toggleButton.setEnabled(true);
        } else {
            Log.d(TAG, "Native libraries loaded successfully!");
            toggleButton.setEnabled(true);
        }

        // Set up OpenGL ES 2.0
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer(this);
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Set up toggle button
        toggleButton.setOnClickListener(v -> {
            Log.d(TAG, "Toggle button clicked!");
            toggleProcessing();
        });
        
        // Initialize button text
        Log.d(TAG, "Initializing button text, isProcessingEnabled: " + isProcessingEnabled);
        updateButtonText();

        // Request camera permission
        if (checkCameraPermission()) {
            initializeCamera();
        } else {
            requestCameraPermission();
        }

        updateStatusText();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_required,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeCamera() {
        cameraHandler = new CameraHandler(this, new CameraHandler.FrameCallback() {
            @Override
            public void onFrameAvailable(byte[] frameData, int width, int height) {
                processFrame(frameData, width, height);
            }
        });
        cameraHandler.startCamera();
    }

    private void processFrame(byte[] frameData, int width, int height) {
        try {
            if (frameData == null || width <= 0 || height <= 0) {
                Log.w(TAG, "Invalid frame data received");
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (lastFrameTime > 0) {
                float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
                if (deltaTime > 0) {
                    currentFps = 1.0f / deltaTime;
                    runOnUiThread(() -> updateFpsDisplay());
                }
            }
            lastFrameTime = currentTime;

            byte[] processedData;
            if (isProcessingEnabled) {
                long startTime = System.nanoTime();
                
                // Try native processing first, fall back to Java if unavailable
                if (NativeProcessor.isLoaded()) {
                    try {
                        processedData = NativeProcessor.processFrame(frameData, width, height);
                        if (processedData == null) {
                            Log.w(TAG, "Native processing returned null, using Java fallback");
                            processedData = processFrameJava(frameData, width, height);
                        }
                    } catch (UnsatisfiedLinkError e) {
                        Log.w(TAG, "Native processing failed, using Java fallback: " + e.getMessage());
                        processedData = processFrameJava(frameData, width, height);
                    }
                } else {
                    // Use Java fallback
                    processedData = processFrameJava(frameData, width, height);
                }
                
                long endTime = System.nanoTime();
                float processingTime = (endTime - startTime) / 1_000_000.0f;
                // Only log occasionally to avoid spam
                if (System.currentTimeMillis() % 1000 < 50) {
                    Log.d(TAG, "Frame processing time: " + processingTime + " ms");
                }
            } else {
                // Use raw frame
                processedData = frameData;
            }

            // Update OpenGL texture
            if (glRenderer != null && processedData != null) {
                glRenderer.updateTexture(processedData, width, height);
                glSurfaceView.requestRender();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing frame", e);
        }
    }

    private void toggleProcessing() {
        isProcessingEnabled = !isProcessingEnabled;
        updateStatusText();
        updateButtonText();
        
        Log.d(TAG, "Processing toggled: " + (isProcessingEnabled ? "ON" : "OFF"));
        
        if (isProcessingEnabled) {
            Toast.makeText(this, "Edge Detection Enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Showing Raw Camera Feed", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Java-based simple edge detection fallback (when native library not available)
     */
    private byte[] processFrameJava(byte[] frameData, int width, int height) {
        byte[] output = new byte[frameData.length];
        
        // Simple grayscale + invert for basic edge effect
        for (int i = 0; i < frameData.length; i += 4) {
            int r = frameData[i] & 0xFF;
            int g = frameData[i + 1] & 0xFF;
            int b = frameData[i + 2] & 0xFF;
            
            // Grayscale
            int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            
            // Simple edge approximation (invert)
            int edge = 255 - gray;
            
            output[i] = (byte)edge;
            output[i + 1] = (byte)edge;
            output[i + 2] = (byte)edge;
            output[i + 3] = (byte)255; // Alpha
        }
        
        return output;
    }
    
    private void updateButtonText() {
        runOnUiThread(() -> {
            if (isProcessingEnabled) {
                toggleButton.setText("Disable Edge Detection");
            } else {
                toggleButton.setText("Enable Edge Detection");
            }
        });
    }

    private void updateStatusText() {
        runOnUiThread(() -> {
            if (isProcessingEnabled) {
                statusTextView.setText(R.string.processing_enabled);
            } else {
                statusTextView.setText(R.string.processing_disabled);
            }
        });
    }

    private void updateFpsDisplay() {
        String fpsText = getString(R.string.fps_label, currentFps);
        fpsTextView.setText(fpsText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraHandler != null) {
            cameraHandler.startCamera();
        }
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraHandler != null) {
            cameraHandler.stopCamera();
        }
        glSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraHandler != null) {
            cameraHandler.release();
        }
    }
}
