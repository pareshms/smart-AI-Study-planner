@echo off
title Smart AI Study Planner - Web Server
setlocal
cd /d "%~dp0"

echo ========================================================
echo        SMART AI STUDY PLANNER - WEB APPLICATION
echo ========================================================
echo.
echo Starting embedded local server...
echo URL: http://localhost:8080
echo.

:: 1. Look for Java in PATH
where java >nul 2>nul
if %errorlevel% equ 0 (
    set JAVA_CMD=java
    goto RUN
)

:: 2. Look in JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
        goto RUN
    )
)

:: 3. Check common Java locations
if exist "C:\Program Files\Java" (
    for /f "delims=" %%I in ('dir /b /s "C:\Program Files\Java\java.exe" 2^>nul') do (
        set JAVA_CMD="%%I"
        goto RUN
    )
)
if exist "C:\Program Files\Eclipse Adoptium" (
    for /f "delims=" %%I in ('dir /b /s "C:\Program Files\Eclipse Adoptium\java.exe" 2^>nul') do (
        set JAVA_CMD="%%I"
        goto RUN
    )
)
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set JAVA_CMD="C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    goto RUN
)

:: 4. Fallback search in user extensions / appdata
if exist "%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe" (
    set JAVA_CMD="%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe"
    goto RUN
)

echo [ERROR] Java was not found on this computer.
echo Please install Java 17 or higher from:
echo https://adoptium.net/
echo (Check the box 'Add to PATH' during install)
echo.
pause
exit /b 1

:RUN
%JAVA_CMD% -cp "out;lib/*" Main %*

echo.
pause
