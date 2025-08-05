#!/bin/bash

# Process all layout files
echo "Processing all layout files for theme attribute references..."

# Find all layout XML files
layout_files=$(find app/src/main/res/layout -name "*.xml")

# Process each file
for file in $layout_files; do
  echo "Processing $file"
  
  # Replace common theme attribute references
  sed -i 's/android:layout_height="?attr\/actionBarSize"/android:layout_height="56dp"/g' "$file"
  sed -i 's/android:background="?attr\/colorPrimary"/android:background="@color\/colorPrimary"/g' "$file"
  sed -i 's/android:textColor="?attr\/colorPrimary"/android:textColor="@color\/colorPrimary"/g' "$file"
  sed -i 's/android:tint="?attr\/colorPrimary"/android:tint="@color\/colorPrimary"/g' "$file"
  
  # Replace android attribute references
  sed -i 's/android:foreground="?android:attr\/selectableItemBackground"/android:foreground="@android:color\/transparent"/g' "$file"
  sed -i 's/android:background="?android:attr\/selectableItemBackground"/android:background="@android:color\/transparent"/g' "$file"
  
  # Replace progressBar styles
  sed -i 's/style="?android:attr\/progressBarStyle"/style="@android:style\/Widget.ProgressBar"/g' "$file"
  
  # Replace theme attribute with direct color
  sed -i 's/android:textColorHint="?attr\/colorTextSecondary"/android:textColorHint="@color\/colorTextSecondary"/g' "$file"
  
  echo "✅ Fixed $file"
done

echo "All layout files processed!"