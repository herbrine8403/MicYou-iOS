@echo off
setlocal

where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle %*
  exit /b %errorlevel%
)

echo Gradle is not installed and the wrapper files are missing.
echo Please add a standard Gradle wrapper or install Gradle locally.
exit /b 1
