@echo off
REM Script to copy OpenCV shared libraries to jniLibs directory

SET OPENCV_SDK=C:\Users\sahil\Downloads\opencv-4.12.0-android-sdk\OpenCV-android-sdk\sdk\native\libs
SET JNILIBS_DIR=app\src\main\jniLibs

echo Copying OpenCV shared libraries...

REM Create jniLibs directories
mkdir "%JNILIBS_DIR%\arm64-v8a" 2>nul
mkdir "%JNILIBS_DIR%\armeabi-v7a" 2>nul
mkdir "%JNILIBS_DIR%\x86" 2>nul
mkdir "%JNILIBS_DIR%\x86_64" 2>nul

REM Copy OpenCV .so files for each ABI
xcopy /Y "%OPENCV_SDK%\arm64-v8a\*.so" "%JNILIBS_DIR%\arm64-v8a\"
xcopy /Y "%OPENCV_SDK%\armeabi-v7a\*.so" "%JNILIBS_DIR%\armeabi-v7a\"
xcopy /Y "%OPENCV_SDK%\x86\*.so" "%JNILIBS_DIR%\x86\"
xcopy /Y "%OPENCV_SDK%\x86_64\*.so" "%JNILIBS_DIR%\x86_64\"

echo.
echo OpenCV libraries copied successfully!
echo.
echo Now:
echo 1. In Android Studio: Build -^> Clean Project
echo 2. File -^> Sync Project with Gradle Files
echo 3. Build -^> Rebuild Project
echo.
pause
