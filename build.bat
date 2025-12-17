@echo off
REM Seasons of Conflict - Build Script
REM Version 1.0.0

echo =========================================
echo   Seasons of Conflict - Build Script
echo   Version 1.0.0
echo =========================================
echo.

REM Check for Java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java not found!
    echo Please install JDK 17 or higher
    pause
    exit /b 1
)

REM Check for Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Maven not found!
    echo Please install Maven 3.6 or higher
    pause
    exit /b 1
)

REM Display versions
echo Checking prerequisites...
java -version 2>&1 | findstr /C:"version"
mvn -version | findstr /C:"Apache Maven"
echo.

REM Clean previous builds
echo Cleaning previous builds...
call mvn clean -q

REM Build the plugin
echo Building plugin...
echo This may take a minute on first build (downloading dependencies)...
echo.

call mvn package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =========================================
    echo BUILD SUCCESSFUL!
    echo =========================================
    echo.
    echo JAR Location: target\SeasonsOfConflict-1.0.0.jar
    
    if exist target\SeasonsOfConflict-1.0.0.jar (
        for %%A in (target\SeasonsOfConflict-1.0.0.jar) do echo JAR Size: %%~zA bytes
        echo.
        echo Next Steps:
        echo 1. Copy JAR to your server plugins folder
        echo 2. Restart your Minecraft server
        echo 3. Configure: plugins\SeasonsOfConflict\config.yml
        echo.
        echo Documentation: README.md
    )
) else (
    echo.
    echo =========================================
    echo BUILD FAILED!
    echo =========================================
    echo.
    echo See error messages above for details.
    echo For help, see BUILD.md
    pause
    exit /b 1
)

pause
