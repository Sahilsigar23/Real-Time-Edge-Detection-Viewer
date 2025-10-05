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

        // Set up OpenGL ES 2.0
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer(this);
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Set up toggle button
        toggleButton.setOnClickListener(v -> toggleProcessing());

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
        long currentTime = System.currentTimeMillis();
        if (lastFrameTime > 0) {
            float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
            currentFps = 1.0f / deltaTime;
            runOnUiThread(() -> updateFpsDisplay());
        }
        lastFrameTime = currentTime;

        byte[] processedData;
        if (isProcessingEnabled) {
            // Process frame through JNI
            long startTime = System.nanoTime();
            processedData = NativeProcessor.processFrame(frameData, width, height);
            long endTime = System.nanoTime();
            float processingTime = (endTime - startTime) / 1_000_000.0f; // Convert to ms
            Log.d(TAG, "Frame processing time: " + processingTime + " ms");
        } else {
            // Use raw frame
            processedData = frameData;
        }

        // Update OpenGL texture
        glRenderer.updateTexture(processedData, width, height);
        glSurfaceView.requestRender();
    }

    private void toggleProcessing() {
        isProcessingEnabled = !isProcessingEnabled;
        updateStatusText();
        Log.d(TAG, "Processing toggled: " + (isProcessingEnabled ? "ON" : "OFF"));
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
