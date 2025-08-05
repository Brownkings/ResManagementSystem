#!/bin/bash

# Files to process
files=(
  "app/src/main/res/drawable/ic_people.xml"
  "app/src/main/res/drawable/ic_inventory.xml"
  "app/src/main/res/drawable/ic_reports.xml"
  "app/src/main/res/drawable/ic_reservation.xml"
  "app/src/main/res/drawable/ic_menu.xml"
  "app/src/main/res/drawable/ic_feedback.xml"
  "app/src/main/res/drawable/ic_calendar.xml"
  "app/src/main/res/drawable/ic_food_placeholder.xml"
  "app/src/main/res/drawable/ic_more_vert.xml"
  "app/src/main/res/drawable/ic_pending_orders.xml"
  "app/src/main/res/drawable/ic_completed_orders.xml"
)

# Process each file
for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    echo "Processing $file"
    
    # Replace ?attr/colorPrimary with @color/colorPrimary
    sed -i 's/android:tint="?attr\/colorPrimary"/android:tint="@color\/colorPrimary"/g' "$file"
    
    # Replace ?android:attr/colorPrimary with @color/colorPrimary
    sed -i 's/android:tint="?android:attr\/colorPrimary"/android:tint="@color\/colorPrimary"/g' "$file"
    
    echo "✅ Fixed $file"
  else
    echo "⚠️ File not found: $file"
  fi
done

echo "All drawable files processed!"