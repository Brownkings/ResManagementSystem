#!/bin/bash

# Files to process
style_files=(
  "app/src/main/res/values/styles.xml"
  "app/src/main/res/values/themes.xml"
  "app/src/main/res/values-night/themes.xml"
)

# Process each file
for file in "${style_files[@]}"; do
  if [ -f "$file" ]; then
    echo "Processing $file"
    
    # Fix any remaining theme attribute references
    sed -i 's/android:layout_height="?attr\/actionBarSize"/android:layout_height="56dp"/g' "$file"
    sed -i 's/"?attr\/colorPrimary"/"@color\/colorPrimary"/g' "$file"
    sed -i 's/"?attr\/colorPrimaryDark"/"@color\/colorPrimaryDark"/g' "$file"
    sed -i 's/"?attr\/colorAccent"/"@color\/colorAccent"/g' "$file"
    sed -i 's/"?attr\/colorTextPrimary"/"@color\/colorTextPrimary"/g' "$file"
    sed -i 's/"?attr\/colorTextSecondary"/"@color\/colorTextSecondary"/g' "$file"
    
    # Fix any references in item tags
    sed -i 's/<item name="android:windowBackground">?attr\/colorBackground<\/item>/<item name="android:windowBackground">@color\/colorBackground<\/item>/g' "$file"
    
    echo "✅ Fixed $file"
  else
    echo "⚠️ File not found: $file"
  fi
done

echo "All style files processed!"