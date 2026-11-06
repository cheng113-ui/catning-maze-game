@echo off
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo Java 17 or newer is required.
    pause
    exit /b 1
)

if not exist build\MazeGame.jar (
    call build.cmd
    if errorlevel 1 (
        pause
        exit /b 1
    )
)

start "" javaw -jar build\MazeGame.jar
