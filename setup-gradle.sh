#!/bin/sh
# Run this once before building if gradle wrapper jar is missing or stub
# This downloads the real gradle-wrapper.jar from official Gradle sources

echo "Downloading Gradle 8.1.1 wrapper..."
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.1.1/gradle/wrapper/gradle-wrapper.jar"
curl -L "$WRAPPER_URL" -o gradle/wrapper/gradle-wrapper.jar
echo "Done! You can now run: ./gradlew assembleDebug"
