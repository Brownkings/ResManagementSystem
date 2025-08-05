#!/bin/bash

echo "Checking for duplicate resource files..."

# Check for duplicate drawable resource names
echo "Checking drawable resources..."
find app/src/main/res -path "*/drawable*/*.xml" -o -path "*/drawable*/*.png" -o -path "*/drawable*/*.jpg" | sort | awk -F/ '{print $NF}' | uniq -d > duplicate_resources.txt

if [ -s duplicate_resources.txt ]; then
    echo "Found duplicate resources:"
    cat duplicate_resources.txt
else
    echo "No duplicate drawable resources found."
fi

# Check for duplicate layout file names
echo "Checking layout resources..."
find app/src/main/res -path "*/layout/*.xml" | sort | awk -F/ '{print $NF}' | uniq -d >> duplicate_resources.txt

if [ -s duplicate_resources.txt ]; then
    echo "Found duplicate resources:"
    cat duplicate_resources.txt
else
    echo "No duplicate layout resources found."
fi

# Check for duplicate string resource IDs
echo "Checking for duplicate string IDs..."
grep -r 'name="' app/src/main/res/values*/strings.xml | sed 's/.*name="//g' | sed 's/".*//g' | sort | uniq -d > duplicate_ids.txt

if [ -s duplicate_ids.txt ]; then
    echo "Found duplicate string IDs:"
    cat duplicate_ids.txt
else
    echo "No duplicate string IDs found."
fi

# Check for duplicate style resource IDs
echo "Checking for duplicate style IDs..."
grep -r 'name="' app/src/main/res/values*/styles.xml | sed 's/.*name="//g' | sed 's/".*//g' | sort | uniq -d > duplicate_styles.txt

if [ -s duplicate_styles.txt ]; then
    echo "Found duplicate style IDs:"
    cat duplicate_styles.txt
else
    echo "No duplicate style IDs found."
fi

echo "Resource check complete."