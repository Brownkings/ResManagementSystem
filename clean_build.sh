#!/bin/bash

echo "===== Cleaning Build Files ====="

# Remove build directories
echo "Removing build directories..."
rm -rf app/build
rm -rf build
rm -rf .gradle

# Check for merged.dir in case it wasn't removed
echo "Ensuring merged.dir is removed..."
rm -rf app/build/intermediates/incremental/debug/mergeDebugResources/merged.dir

echo "✅ Build files cleaned successfully"

echo ""
echo "===== Checking for resources.ap_ files ====="
# These are compiled resource files that might still have problems
find . -name "resources.ap_" -delete
echo "✅ Removed any compiled resource files"

echo ""
echo "===== Clean Complete ====="
echo "You can now rebuild the project with a clean state."