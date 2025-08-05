#!/bin/bash

echo "===== Fixing values.xml Resource Compilation Issues ====="

# Find all XML files in values directories
values_files=$(find app/src/main/res/values* -name "*.xml")

echo "Processing the following values XML files:"
for file in $values_files; do
  echo "- $file"
done

echo ""
echo "Fixing theme attribute references in values files..."
for file in $values_files; do
  echo "Processing $file"
  
  # Replace theme attribute references with direct color references
  sed -i 's/?attr\/colorPrimary/@color\/colorPrimary/g' "$file"
  sed -i 's/?attr\/colorPrimaryDark/@color\/colorPrimaryDark/g' "$file"
  sed -i 's/?attr\/colorAccent/@color\/colorAccent/g' "$file"
  sed -i 's/?attr\/colorControlNormal/@color\/colorControlNormal/g' "$file"
  sed -i 's/?attr\/colorControlActivated/@color\/colorControlActivated/g' "$file"
  sed -i 's/?attr\/colorControlHighlight/@color\/colorControlHighlight/g' "$file"
  sed -i 's/?attr\/colorButtonNormal/@color\/colorButtonNormal/g' "$file"
  sed -i 's/?attr\/colorSurface/@color\/colorSurface/g' "$file"
  sed -i 's/?attr\/colorOnSurface/@color\/colorOnSurface/g' "$file"
  sed -i 's/?attr\/colorError/@color\/colorError/g' "$file"
  
  # Replace Android theme attributes with direct resource references
  sed -i 's/?android:attr\/selectableItemBackground/@drawable\/selectable_item_background/g' "$file"
  sed -i 's/?android:attr\/actionBarSize/56dp/g' "$file"
  sed -i 's/?android:attr\/textColorPrimary/@color\/textColorPrimary/g' "$file"
  sed -i 's/?android:attr\/textColorSecondary/@color\/textColorSecondary/g' "$file"
  
  echo "✅ Fixed $file"
done

# Check for any remaining theme attribute references
echo ""
echo "Checking for remaining theme attribute references in values files..."
remaining_refs=$(grep -r "?attr\/" app/src/main/res/values* || echo "None")
if [ "$remaining_refs" == "None" ]; then
  echo "✅ No remaining ?attr/ references found in values files."
else
  echo "⚠️ Found ?attr/ references in values files:"
  echo "$remaining_refs"
fi

# Check for any remaining Android theme attribute references
echo ""
echo "Checking for remaining Android theme attribute references in values files..."
remaining_android_refs=$(grep -r "?android:attr\/" app/src/main/res/values* || echo "None")
if [ "$remaining_android_refs" == "None" ]; then
  echo "✅ No remaining ?android:attr/ references found in values files."
else
  echo "⚠️ Found ?android:attr/ references in values files:"
  echo "$remaining_android_refs"
fi

echo ""
echo "===== Values XML Fixing Complete ====="