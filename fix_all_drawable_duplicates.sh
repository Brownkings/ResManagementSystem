#!/bin/bash

echo "===== Fixing All Duplicate Drawables ====="

# Create a temporary file to store duplicate drawables
temp_file=$(mktemp)

# Find all XML files in drawable directories
find app/src/main/res/drawable* -type f -name "*.xml" -exec basename {} \; | sort | uniq -d > "$temp_file"

# Process each duplicate drawable
if [ -s "$temp_file" ]; then
    echo "Found $(wc -l < "$temp_file") duplicate drawable files."
    
    while read -r drawable; do
        echo "Processing duplicate: $drawable"
        
        # Find all instances of this drawable
        locations=($(find app/src/main/res/drawable* -name "$drawable"))
        
        if [ ${#locations[@]} -gt 1 ]; then
            echo "Found in ${#locations[@]} locations:"
            
            for loc in "${locations[@]}"; do
                echo "  $loc"
            done
            
            # Keep the one in the main drawable directory if it exists
            keep=""
            for loc in "${locations[@]}"; do
                if [[ "$loc" == "app/src/main/res/drawable/$drawable" ]]; then
                    keep="$loc"
                    break
                fi
            done
            
            # If not found in the main drawable directory, keep the first one
            if [ -z "$keep" ]; then
                keep="${locations[0]}"
            fi
            
            echo "Keeping: $keep"
            
            # Remove all others
            for loc in "${locations[@]}"; do
                if [ "$loc" != "$keep" ]; then
                    echo "Removing: $loc"
                    rm "$loc"
                fi
            done
        fi
        
        echo ""
    done < "$temp_file"
else
    echo "No duplicate drawables found."
fi

# Clean up
rm "$temp_file"

echo "===== Duplicate Drawable Fix Complete ====="