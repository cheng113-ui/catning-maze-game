@echo off
cd /d "%~dp0"

if not exist build mkdir build
if not exist build\classes mkdir build\classes
if not exist build\test-classes mkdir build\test-classes
if not exist build\classes\images mkdir build\classes\images

copy /Y src\main\resources\images\*.png build\classes\images\ >nul

dir /s /b src\main\java\*.java > build\sources.txt
javac --release 17 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 goto failed

dir /s /b src\test\java\*.java > build\test-sources.txt
javac --release 17 -encoding UTF-8 -cp build\classes -d build\test-classes @build\test-sources.txt
if errorlevel 1 goto failed

java -cp "build\classes;build\test-classes" com.zhengli.maze.MazeModelTest
if errorlevel 1 goto failed

jar --create --file build\MazeGame.jar --main-class com.zhengli.maze.MazeGame -C build\classes .
if errorlevel 1 goto failed

echo Build complete: build\MazeGame.jar
exit /b 0

:failed
echo Build failed.
exit /b 1
