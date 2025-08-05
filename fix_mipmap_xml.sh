#!/bin/bash

# Find all XML files in mipmap directories
mipmap_files=$(find app/src/main/res/mipmap* -name "*.xml")

# Process each file
for file in $mipmap_files; do
  echo "Processing $file"
  
  # Fix background references
  sed -i 's/@drawable\/ic_launcher_background/@color\/ic_launcher_background/g' "$file"
  
  # Fix any theme attribute references
  sed -i 's/"?attr\/colorPrimary"/"@color\/colorPrimary"/g' "$file"
  
  echo "✅ Fixed $file"
done

echo "All mipmap XML files processed!"