#!/bin/bash

echo "===== Fixing Duplicate Drawables ====="

# Check if ic_feedback.xml exists in both directories
if [ -f "app/src/main/res/drawable/ic_feedback.xml" ] && [ -f "app/src/main/res/drawable-v24/ic_feedback.xml" ]; then
  echo "Found duplicate ic_feedback.xml files."
  
  # Compare the files to ensure they are identical
  if diff -q "app/src/main/res/drawable/ic_feedback.xml" "app/src/main/res/drawable-v24/ic_feedback.xml" >/dev/null; then
    echo "Files are identical. Removing the one in drawable-v24 directory."
    rm "app/src/main/res/drawable-v24/ic_feedback.xml"
    echo "Removed app/src/main/res/drawable-v24/ic_feedback.xml"
  else
    echo "Files are different. Please check the content manually."
    echo "File 1: app/src/main/res/drawable/ic_feedback.xml"
    echo "File 2: app/src/main/res/drawable-v24/ic_feedback.xml"
  fi
else
  echo "No duplicate ic_feedback.xml files found."
fi

echo "===== Fix Complete ====="