#!/bin/bash

echo "Checking for pluginManagement blocks in build.gradle files..."
find . -name "build.gradle" | xargs grep -l "pluginManagement" || echo "No pluginManagement blocks found in build.gradle files."

echo ""
echo "Checking for other potential Gradle configuration issues..."
find . -name "*.gradle" | grep -v "settings.gradle" | xargs grep -l "RepositoriesMode" || echo "No RepositoriesMode references found in non-settings.gradle files."

echo ""
echo "Checking for additional gradle files..."
find . -name "*.gradle" | sort