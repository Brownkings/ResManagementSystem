package com.spu.restaurantmanagementsystem.manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.adapters.MenuItemAdapter;
import com.spu.restaurantmanagementsystem.models.MenuItem;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManagementActivity extends AppCompatActivity implements MenuItemAdapter.OnMenuItemClickListener {

    private RecyclerView menuItemsRecyclerView;
    private FloatingActionButton addMenuItemFab;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    
    private MenuItemAdapter adapter;
    private List<MenuItem> menuItems;
    
    private DatabaseReference menuItemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_management);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Menu Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        menuItemsRef = FirebaseUtil.getMenuItemsRef();
        
        // Initialize UI elements
        menuItemsRecyclerView = findViewById(R.id.rv_menu_items);
        addMenuItemFab = findViewById(R.id.fab_add_menu_item);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        
        // Set up RecyclerView
        menuItems = new ArrayList<>();
        adapter = new MenuItemAdapter(this, menuItems, this);
        menuItemsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        menuItemsRecyclerView.setAdapter(adapter);
        
        // Set up FAB
        addMenuItemFab.setOnClickListener(v -> showAddEditMenuItemDialog(null));
        
        // Load menu items
        loadMenuItems();
    }

    private void loadMenuItems() {
        progressBar.setVisibility(View.VISIBLE);
        
        menuItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuItems.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MenuItem menuItem = snapshot.getValue(MenuItem.class);
                    if (menuItem != null) {
                        menuItems.add(menuItem);
                    }
                }
                
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                
                // Show/hide empty state
                if (menuItems.isEmpty()) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MenuManagementActivity.this, 
                        "Error loading menu items: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        // Show dialog to edit or delete the menu item
        showAddEditMenuItemDialog(menuItem);
    }
    
    private void showAddEditMenuItemDialog(MenuItem existingItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_menu_item, null);
        builder.setView(dialogView);
        
        // Initialize dialog elements
        EditText nameEditText = dialogView.findViewById(R.id.et_name);
        EditText descriptionEditText = dialogView.findViewById(R.id.et_description);
        EditText priceEditText = dialogView.findViewById(R.id.et_price);
        EditText imageUrlEditText = dialogView.findViewById(R.id.et_image_url);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        CheckBox availabilityCheckBox = dialogView.findViewById(R.id.cb_availability);
        Button saveButton = dialogView.findViewById(R.id.btn_save);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        Button deleteButton = dialogView.findViewById(R.id.btn_delete);
        
        // Set up category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Constants.getMenuCategories());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Set dialog title and button visibility based on mode
        String dialogTitle;
        if (existingItem != null) {
            dialogTitle = "Edit Menu Item";
            deleteButton.setVisibility(View.VISIBLE);
            
            // Pre-fill fields
            nameEditText.setText(existingItem.getName());
            descriptionEditText.setText(existingItem.getDescription());
            priceEditText.setText(String.format("%.2f", existingItem.getPrice()));
            imageUrlEditText.setText(existingItem.getImageUrl());
            availabilityCheckBox.setChecked(existingItem.isAvailability());
            
            // Set category
            int categoryPosition = Constants.getMenuCategories().indexOf(existingItem.getCategory());
            if (categoryPosition >= 0) {
                categorySpinner.setSelection(categoryPosition);
            }
        } else {
            dialogTitle = "Add New Menu Item";
            deleteButton.setVisibility(View.GONE);
            
            // Set default values
            availabilityCheckBox.setChecked(true);
        }
        
        builder.setTitle(dialogTitle);
        
        AlertDialog dialog = builder.create();
        
        // Set up button click listeners
        saveButton.setOnClickListener(v -> {
            if (validateMenuItemInput(nameEditText, descriptionEditText, priceEditText)) {
                // Create or update menu item
                saveMenuItem(
                        existingItem,
                        nameEditText.getText().toString().trim(),
                        descriptionEditText.getText().toString().trim(),
                        priceEditText.getText().toString().trim(),
                        imageUrlEditText.getText().toString().trim(),
                        categorySpinner.getSelectedItem().toString(),
                        availabilityCheckBox.isChecked()
                );
                dialog.dismiss();
            }
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        if (existingItem != null) {
            deleteButton.setOnClickListener(v -> {
                // Show confirm delete dialog
                new AlertDialog.Builder(this)
                        .setTitle("Delete Menu Item")
                        .setMessage("Are you sure you want to delete this menu item?")
                        .setPositiveButton("Delete", (confirmDialog, which) -> {
                            deleteMenuItem(existingItem);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
        
        dialog.show();
    }
    
    private boolean validateMenuItemInput(EditText nameEditText, EditText descriptionEditText, EditText priceEditText) {
        boolean isValid = true;
        
        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameEditText.setError("Name is required");
            isValid = false;
        }
        
        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            descriptionEditText.setError("Description is required");
            isValid = false;
        }
        
        String priceStr = priceEditText.getText().toString().trim();
        if (priceStr.isEmpty()) {
            priceEditText.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    priceEditText.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                priceEditText.setError("Invalid price format");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void saveMenuItem(MenuItem existingItem, String name, String description, 
                             String priceStr, String imageUrl, String category, boolean availability) {
        progressBar.setVisibility(View.VISIBLE);
        
        double price = Double.parseDouble(priceStr);
        
        String itemId;
        if (existingItem != null) {
            // Update existing item
            itemId = existingItem.getItemId();
        } else {
            // Create new item
            itemId = FirebaseUtil.generateKey(menuItemsRef);
        }
        
        MenuItem menuItem = new MenuItem(
                itemId,
                name,
                description,
                price,
                availability,
                category,
                imageUrl
        );
        
        // If updating, preserve the ingredients mapping
        if (existingItem != null && existingItem.getIngredientsUsed() != null) {
            menuItem.setIngredientsUsed(existingItem.getIngredientsUsed());
        }
        
        // Save to Firebase
        menuItemsRef.child(itemId).setValue(menuItem)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MenuManagementActivity.this, 
                            "Menu item saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MenuManagementActivity.this, 
                            "Failed to save menu item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void deleteMenuItem(MenuItem menuItem) {
        progressBar.setVisibility(View.VISIBLE);
        
        menuItemsRef.child(menuItem.getItemId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MenuManagementActivity.this, 
                            "Menu item deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MenuManagementActivity.this, 
                            "Failed to delete menu item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
