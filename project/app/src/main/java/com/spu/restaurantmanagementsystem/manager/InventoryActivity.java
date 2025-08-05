package com.spu.restaurantmanagementsystem.manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.spu.restaurantmanagementsystem.adapters.InventoryAdapter;
import com.spu.restaurantmanagementsystem.models.InventoryItem;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnInventoryItemClickListener {

    private RecyclerView inventoryRecyclerView;
    private FloatingActionButton addItemFab;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    
    private InventoryAdapter adapter;
    private List<InventoryItem> inventoryItems;
    
    private DatabaseReference inventoryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Inventory Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        inventoryRef = FirebaseUtil.getInventoryRef();
        
        // Initialize UI elements
        inventoryRecyclerView = findViewById(R.id.rv_inventory);
        addItemFab = findViewById(R.id.fab_add_inventory);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        
        // Set up RecyclerView
        inventoryItems = new ArrayList<>();
        adapter = new InventoryAdapter(this, inventoryItems, this, true);
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inventoryRecyclerView.setAdapter(adapter);
        
        // Set up FAB
        addItemFab.setOnClickListener(v -> showAddEditInventoryDialog(null));
        
        // Load inventory items
        loadInventoryItems();
    }

    private void loadInventoryItems() {
        progressBar.setVisibility(View.VISIBLE);
        
        inventoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inventoryItems.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    InventoryItem item = snapshot.getValue(InventoryItem.class);
                    if (item != null) {
                        inventoryItems.add(item);
                    }
                }
                
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                
                // Show/hide empty state
                if (inventoryItems.isEmpty()) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InventoryActivity.this, 
                        "Error loading inventory: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInventoryItemClick(InventoryItem inventoryItem) {
        // Show inventory details dialog
        showInventoryDetailsDialog(inventoryItem);
    }

    @Override
    public void onEditItemClick(InventoryItem inventoryItem) {
        // Show dialog to edit inventory item
        showAddEditInventoryDialog(inventoryItem);
    }
    
    private void showInventoryDetailsDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_inventory_details, null);
        builder.setView(dialogView);
        
        // Initialize dialog elements
        TextView nameTextView = dialogView.findViewById(R.id.tv_item_name);
        TextView quantityTextView = dialogView.findViewById(R.id.tv_quantity);
        TextView unitCostTextView = dialogView.findViewById(R.id.tv_unit_cost);
        TextView totalValueTextView = dialogView.findViewById(R.id.tv_total_value);
        TextView thresholdTextView = dialogView.findViewById(R.id.tv_reorder_threshold);
        TextView statusTextView = dialogView.findViewById(R.id.tv_status);
        Button editButton = dialogView.findViewById(R.id.btn_edit);
        Button closeButton = dialogView.findViewById(R.id.btn_close);
        
        // Set dialog data
        nameTextView.setText(item.getItemName());
        quantityTextView.setText(item.getFormattedQuantity());
        unitCostTextView.setText(item.getFormattedCost());
        totalValueTextView.setText("Total Value: " + item.getFormattedValue());
        thresholdTextView.setText("Reorder When Below: " + item.getReorderThreshold() + " " + item.getUnit());
        
        statusTextView.setText("Status: " + item.getStockStatus());
        if (item.isLowStock()) {
            statusTextView.setTextColor(getResources().getColor(R.color.colorLowStock));
        } else {
            statusTextView.setTextColor(getResources().getColor(R.color.colorAvailable));
        }
        
        builder.setTitle("Inventory Item Details");
        
        AlertDialog dialog = builder.create();
        
        editButton.setOnClickListener(v -> {
            dialog.dismiss();
            showAddEditInventoryDialog(item);
        });
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void showAddEditInventoryDialog(InventoryItem existingItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_inventory, null);
        builder.setView(dialogView);
        
        // Initialize dialog elements
        EditText nameEditText = dialogView.findViewById(R.id.et_item_name);
        EditText quantityEditText = dialogView.findViewById(R.id.et_quantity);
        EditText costEditText = dialogView.findViewById(R.id.et_cost);
        EditText thresholdEditText = dialogView.findViewById(R.id.et_threshold);
        Spinner unitSpinner = dialogView.findViewById(R.id.spinner_unit);
        Button saveButton = dialogView.findViewById(R.id.btn_save);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        Button deleteButton = dialogView.findViewById(R.id.btn_delete);
        
        // Set up unit spinner
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Constants.getInventoryUnits());
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        
        // Set dialog title and button visibility based on mode
        String dialogTitle;
        if (existingItem != null) {
            dialogTitle = "Edit Inventory Item";
            deleteButton.setVisibility(View.VISIBLE);
            
            // Pre-fill fields
            nameEditText.setText(existingItem.getItemName());
            quantityEditText.setText(String.valueOf(existingItem.getQuantityInStock()));
            costEditText.setText(String.format("%.2f", existingItem.getUnitCost()));
            thresholdEditText.setText(String.valueOf(existingItem.getReorderThreshold()));
            
            // Set unit
            int unitPosition = Constants.getInventoryUnits().indexOf(existingItem.getUnit());
            if (unitPosition >= 0) {
                unitSpinner.setSelection(unitPosition);
            }
        } else {
            dialogTitle = "Add New Inventory Item";
            deleteButton.setVisibility(View.GONE);
            
            // Set default values
            thresholdEditText.setText("10");
        }
        
        builder.setTitle(dialogTitle);
        
        AlertDialog dialog = builder.create();
        
        // Set up button click listeners
        saveButton.setOnClickListener(v -> {
            if (validateInventoryInput(nameEditText, quantityEditText, costEditText, thresholdEditText)) {
                // Create or update inventory item
                saveInventoryItem(
                        existingItem,
                        nameEditText.getText().toString().trim(),
                        Integer.parseInt(quantityEditText.getText().toString().trim()),
                        Double.parseDouble(costEditText.getText().toString().trim()),
                        Integer.parseInt(thresholdEditText.getText().toString().trim()),
                        unitSpinner.getSelectedItem().toString()
                );
                dialog.dismiss();
            }
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        if (existingItem != null) {
            deleteButton.setOnClickListener(v -> {
                // Show confirm delete dialog
                new AlertDialog.Builder(this)
                        .setTitle("Delete Inventory Item")
                        .setMessage("Are you sure you want to delete this inventory item?")
                        .setPositiveButton("Delete", (confirmDialog, which) -> {
                            deleteInventoryItem(existingItem);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
        
        dialog.show();
    }
    
    private boolean validateInventoryInput(EditText nameEditText, EditText quantityEditText, 
                                        EditText costEditText, EditText thresholdEditText) {
        boolean isValid = true;
        
        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameEditText.setError("Name is required");
            isValid = false;
        }
        
        String quantityStr = quantityEditText.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            quantityEditText.setError("Quantity is required");
            isValid = false;
        } else {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    quantityEditText.setError("Quantity cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                quantityEditText.setError("Invalid quantity format");
                isValid = false;
            }
        }
        
        String costStr = costEditText.getText().toString().trim();
        if (costStr.isEmpty()) {
            costEditText.setError("Cost is required");
            isValid = false;
        } else {
            try {
                double cost = Double.parseDouble(costStr);
                if (cost <= 0) {
                    costEditText.setError("Cost must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                costEditText.setError("Invalid cost format");
                isValid = false;
            }
        }
        
        String thresholdStr = thresholdEditText.getText().toString().trim();
        if (thresholdStr.isEmpty()) {
            thresholdEditText.setError("Threshold is required");
            isValid = false;
        } else {
            try {
                int threshold = Integer.parseInt(thresholdStr);
                if (threshold < 0) {
                    thresholdEditText.setError("Threshold cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                thresholdEditText.setError("Invalid threshold format");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void saveInventoryItem(InventoryItem existingItem, String name, int quantity, 
                                double cost, int threshold, String unit) {
        progressBar.setVisibility(View.VISIBLE);
        
        String itemId;
        Date lastUpdated = new Date();
        
        if (existingItem != null) {
            // Update existing item
            itemId = existingItem.getItemId();
            lastUpdated = existingItem.getLastUpdated();
        } else {
            // Create new item
            itemId = FirebaseUtil.generateKey(inventoryRef);
        }
        
        InventoryItem inventoryItem = new InventoryItem(
                itemId,
                name,
                quantity,
                threshold,
                lastUpdated,
                unit,
                cost
        );
        
        // Save to Firebase
        inventoryRef.child(itemId).setValue(inventoryItem)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InventoryActivity.this, 
                            "Inventory item saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InventoryActivity.this, 
                            "Failed to save inventory item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void deleteInventoryItem(InventoryItem inventoryItem) {
        progressBar.setVisibility(View.VISIBLE);
        
        inventoryRef.child(inventoryItem.getItemId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InventoryActivity.this, 
                            "Inventory item deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InventoryActivity.this, 
                            "Failed to delete inventory item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
