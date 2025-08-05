#!/bin/bash

echo "Creating backup of the project directory..."
mkdir -p project_backup
cp -r project/* project_backup/ 2>/dev/null || echo "No files to backup"

echo "Checking if there are any unique files in the project directory that don't exist in the root..."
for file in $(find project -type f -not -path "*/\.*" | sort); do
  relative_path=${file#project/}
  if [ ! -f "$relative_path" ]; then
    echo "Found unique file: $file"
    # You might want to manually move important files later
  fi
done

echo "=== Fixing Gradle Configuration ==="
echo "1. Ensure the root build.gradle is correct"
cat build.gradle

echo "2. Ensure the root settings.gradle is correct"
cat settings.gradle

echo "3. Verify app/build.gradle is correct"
cat app/build.gradle 2>/dev/null || echo "app/build.gradle doesn't exist"

echo "=== Project Structure Check Complete ==="
echo "Note: You may want to manually examine any unique files in the project directory"
echo "or consider removing the project directory if it's a duplicate/old version."