#!/bin/bash

# Fix values-night theme references
echo "Processing values-night theme files..."

# Process night theme
if [ -f "app/src/main/res/values-night/themes.xml" ]; then
  echo "Processing app/src/main/res/values-night/themes.xml"
  
  # Make sure colorPrimary is consistently referenced
  sed -i 's/<item name="colorPrimary">?attr\/colorPrimary<\/item>/<item name="colorPrimary">@color\/colorPrimary<\/item>/g' "app/src/main/res/values-night/themes.xml"
  
  # Make sure colorPrimaryDark is consistently referenced
  sed -i 's/<item name="colorPrimaryDark">?attr\/colorPrimaryDark<\/item>/<item name="colorPrimaryDark">@color\/colorPrimaryDark<\/item>/g' "app/src/main/res/values-night/themes.xml"
  
  # Make sure colorAccent is consistently referenced
  sed -i 's/<item name="colorAccent">?attr\/colorAccent<\/item>/<item name="colorAccent">@color\/colorAccent<\/item>/g' "app/src/main/res/values-night/themes.xml"
  
  echo "✅ Fixed app/src/main/res/values-night/themes.xml"
else
  echo "⚠️ File not found: app/src/main/res/values-night/themes.xml"
fi

echo "Night theme files processed!"