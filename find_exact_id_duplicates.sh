#!/bin/bash

echo "===== Finding Exact ID Duplicates ====="

# Create a temporary file to store the IDs and their file locations
temp_file=$(mktemp)

# Find all XML files in the layout directories
find app/src/main/res/layout* -type f -name "*.xml" | while read -r file; do
  # Extract all IDs from the file and save them with the filename
  grep -o 'android:id="@+id/[^"]*"' "$file" | sed 's/android:id="@+id\///g' | sed 's/"//g' | while read -r id; do
    echo "$id $file" >> "$temp_file"
  done
done

# Sort the file by ID and then count occurrences
echo "Duplicate IDs found:"
sort "$temp_file" | awk '{print $1}' | uniq -c | sort -nr | awk '$1 > 1 {print $2}' | while read -r dup_id; do
  echo "ID: $dup_id appears in:"
  grep "^$dup_id " "$temp_file" | awk '{print "  " $2}'
done

# Clean up
rm "$temp_file"

echo "===== Search Complete ====="