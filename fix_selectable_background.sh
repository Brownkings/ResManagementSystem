#!/bin/bash

# Files to process
files=(
  "app/src/main/res/layout/activity_waiter_dashboard.xml"
  "app/src/main/res/layout/activity_chef_dashboard.xml"
  "app/src/main/res/layout/activity_customer_dashboard.xml"
)

# Process each file
for file in "${files[@]}"; do
  echo "Processing $file"
  
  # Replace ?android:attr/selectableItemBackground with @android:color/transparent
  sed -i 's/android:foreground="?android:attr\/selectableItemBackground"/android:foreground="@android:color\/transparent"/g' "$file"
  
  echo "✅ Fixed $file"
done

echo "All files processed!"