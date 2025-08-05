#!/bin/bash

echo "===== Final Resource Check ====="

# Check for any remaining theme attribute references
echo "Checking for remaining '?attr/' references..."
grep_result=$(find app/src/main/res -name "*.xml" | xargs grep -l "?attr/")
if [ -z "$grep_result" ]; then
  echo "✅ No '?attr/' references found."
else
  echo "⚠️ Found '?attr/' references in the following files:"
  echo "$grep_result"
fi

# Check for android attr references
echo "Checking for remaining '?android:attr/' references..."
grep_result=$(find app/src/main/res -name "*.xml" | xargs grep -l "?android:attr/")
if [ -z "$grep_result" ]; then
  echo "✅ No '?android:attr/' references found."
else
  echo "⚠️ Found '?android:attr/' references in the following files:"
  echo "$grep_result"
fi

# Check for drawable references to ic_launcher_background
echo "Checking for '@drawable/ic_launcher_background' references..."
grep_result=$(find app/src/main/res -name "*.xml" | xargs grep -l "@drawable/ic_launcher_background")
if [ -z "$grep_result" ]; then
  echo "✅ No '@drawable/ic_launcher_background' references found."
else
  echo "⚠️ Found '@drawable/ic_launcher_background' references in the following files:"
  echo "$grep_result"
fi

echo "===== Check Complete ====="