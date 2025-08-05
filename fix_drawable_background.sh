#!/bin/bash

echo "Checking for missing drawable references in layout files..."

# Find all layout XML files
layout_files=$(find app/src/main/res/layout* -name "*.xml")

# Function to fix drawable references
fix_references() {
  for file in $layout_files; do
    echo "Checking $file..."
    
    # Find and fix android:drawable references
    if grep -q "android:src=\"@android:drawable/ic_menu_" "$file"; then
      echo "Found android:src drawable references in $file"
      
      # Replace ic_menu_remove with custom ic_remove
      if grep -q "android:src=\"@android:drawable/ic_menu_remove\"" "$file"; then
        echo "Replacing ic_menu_remove with ic_remove in $file"
        sed -i 's/android:src="@android:drawable\/ic_menu_remove"/android:src="@drawable\/ic_remove"/g' "$file"
      fi
      
      # Replace ic_menu_add with custom ic_add
      if grep -q "android:src=\"@android:drawable/ic_menu_add\"" "$file"; then
        echo "Replacing ic_menu_add with ic_add in $file"
        sed -i 's/android:src="@android:drawable\/ic_menu_add"/android:src="@drawable\/ic_add"/g' "$file"
      fi
      
      # Replace ic_menu_delete with custom ic_delete
      if grep -q "android:src=\"@android:drawable/ic_menu_delete\"" "$file"; then
        echo "Replacing ic_menu_delete with ic_delete in $file"
        sed -i 's/android:src="@android:drawable\/ic_menu_delete"/android:src="@drawable\/ic_delete"/g' "$file"
      fi
    fi
    
    # Check for android:background drawable references
    if grep -q "android:background=\"@android:drawable/" "$file"; then
      echo "Found android:background drawable references in $file"
      # Add specific replacements here if needed
    fi
  done
}

# Execute the function
fix_references

echo "Reference check complete."