#!/bin/bash

# List of files to process
files=(
  "app/src/main/res/layout/activity_menu_management.xml"
  "app/src/main/res/layout/activity_manager_dashboard.xml"
  "app/src/main/res/layout/activity_chef_dashboard.xml"
  "app/src/main/res/layout/activity_order_list.xml"
  "app/src/main/res/layout/activity_view_menu.xml"
  "app/src/main/res/layout/activity_customer_reservations.xml"
  "app/src/main/res/layout/activity_customer_feedback.xml"
  "app/src/main/res/layout/activity_customer_dashboard.xml"
)

# Process each file
for file in "${files[@]}"; do
  echo "Processing $file"
  
  # Replace ?attr/actionBarSize with 56dp
  sed -i 's/android:layout_height="?attr\/actionBarSize"/android:layout_height="56dp"/g' "$file"
  
  # Replace ?attr/colorPrimary with @color/colorPrimary
  sed -i 's/android:background="?attr\/colorPrimary"/android:background="@color\/colorPrimary"/g' "$file"
  
  # Replace any other ?attr/colorPrimary references
  sed -i 's/android:textColor="?attr\/colorPrimary"/android:textColor="@color\/colorPrimary"/g' "$file"
  sed -i 's/android:tint="?attr\/colorPrimary"/android:tint="@color\/colorPrimary"/g' "$file"
  
  echo "✅ Fixed $file"
done

echo "All files processed!"