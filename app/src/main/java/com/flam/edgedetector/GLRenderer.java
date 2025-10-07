package com.flam.edgedetector;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL ES 2.0 Renderer for displaying camera frames as textures
 */
public class GLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "GLRenderer";

    // Vertex shader - simple pass-through with texture coordinates
    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "    vTexCoord = aTexCoord;\n" +
            "}\n";

    // Fragment shader - sample texture
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "varying vec2 vTexCoord;\n" +
            "uniform sampler2D uTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(uTexture, vTexCoord);\n" +
            "}\n";

    // Vertex coordinates (full screen quad)
    private static final float[] VERTEX_COORDS = {
            -1.0f, -1.0f,  // Bottom left
             1.0f, -1.0f,  // Bottom right
            -1.0f,  1.0f,  // Top left
             1.0f,  1.0f   // Top right
    };

    // Texture coordinates (rotated 90 degrees counterclockwise for camera orientation)
    private static final float[] TEXTURE_COORDS = {
            1.0f, 1.0f,  // Bottom left
            1.0f, 0.0f,  // Bottom right
            0.0f, 1.0f,  // Top left
            0.0f, 0.0f   // Top right
    };

    private final Context context;
    private int program;
    private int textureId;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    
    private int aPositionHandle;
    private int aTexCoordHandle;
    private int uTextureHandle;
    
    private byte[] currentFrameData;
    private int frameWidth;
    private int frameHeight;
    private final Object frameLock = new Object();

    public GLRenderer(Context context) {
        this.context = context;
        initBuffers();
    }

    private void initBuffers() {
        // Initialize vertex buffer
        vertexBuffer = ByteBuffer.allocateDirect(VERTEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(VERTEX_COORDS);
        vertexBuffer.position(0);

        // Initialize texture coordinate buffer
        textureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureBuffer.put(TEXTURE_COORDS);
        textureBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // Create shader program
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (program == 0) {
            Log.e(TAG, "Failed to create shader program");
            return;
        }

        // Get attribute and uniform locations
        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        aTexCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        uTextureHandle = GLES20.glGetUniformLocation(program, "uTexture");

        // Generate texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        Log.d(TAG, "OpenGL surface created successfully");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Log.d(TAG, "Surface changed: " + width + "x" + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        synchronized (frameLock) {
            if (currentFrameData != null && frameWidth > 0 && frameHeight > 0) {
                // Update texture with new frame data
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
                ByteBuffer buffer = ByteBuffer.wrap(currentFrameData);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                        frameWidth, frameHeight, 0, GLES20.GL_RGBA,
                        GLES20.GL_UNSIGNED_BYTE, buffer);
            }
        }

        // Use shader program
        GLES20.glUseProgram(program);

        // Set vertex positions
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Set texture coordinates
        GLES20.glEnableVertexAttribArray(aTexCoordHandle);
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(uTextureHandle, 0);

        // Draw quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Cleanup
        GLES20.glDisableVertexAttribArray(aPositionHandle);
        GLES20.glDisableVertexAttribArray(aTexCoordHandle);

        checkGLError("onDrawFrame");
    }

    /**
     * Update texture with new frame data
     */
    public void updateTexture(byte[] frameData, int width, int height) {
        synchronized (frameLock) {
            currentFrameData = frameData;
            frameWidth = width;
            frameHeight = height;
        }
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            Log.e(TAG, "Failed to create program");
            return 0;
        }

        GLES20.glAttachShader(program, vertexShader);
        checkGLError("glAttachShader vertex");
        
        GLES20.glAttachShader(program, fragmentShader);
        checkGLError("glAttachShader fragment");
        
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Failed to link program: " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            return 0;
        }

        return program;
    }

    private int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            Log.e(TAG, "Failed to create shader");
            return 0;
        }

        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Failed to compile shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private void checkGLError(String operation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, operation + ": glError " + error);
        }
    }
}
