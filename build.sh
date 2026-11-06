#!/usr/bin/env sh
set -eu

mkdir -p build/classes build/test-classes build/classes/images
cp src/main/resources/images/*.png build/classes/images/

find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d build/classes @build/sources.txt

find src/test/java -name '*.java' > build/test-sources.txt
javac --release 17 -encoding UTF-8 -cp build/classes \
  -d build/test-classes @build/test-sources.txt

java -cp "build/classes:build/test-classes" com.zhengli.maze.MazeModelTest

jar --create --file build/MazeGame.jar \
  --main-class com.zhengli.maze.MazeGame \
  -C build/classes .

echo "Build complete: build/MazeGame.jar"
