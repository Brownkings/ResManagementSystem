package com.spu.restaurantmanagementsystem.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.List;

public class MenuBrowseActivity extends AppCompatActivity implements MenuItemAdapter.OnMenuItemClickListener {

    private RecyclerView menuRecyclerView;
    private Spinner categorySpinner;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    
    private MenuItemAdapter adapter;
    private List<MenuItem> menuItems;
    private List<MenuItem> filteredMenuItems;
    
    private DatabaseReference menuItemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_browse);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Menu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        menuItemsRef = FirebaseUtil.getMenuItemsRef();
        
        // Initialize UI elements
        menuRecyclerView = findViewById(R.id.rv_menu);
        categorySpinner = findViewById(R.id.spinner_category);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        
        // Set up RecyclerView
        menuItems = new ArrayList<>();
        filteredMenuItems = new ArrayList<>();
        adapter = new MenuItemAdapter(this, filteredMenuItems, this);
        menuRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        menuRecyclerView.setAdapter(adapter);
        
        // Set up category spinner
        setupCategorySpinner();
        
        // Load menu items
        loadMenuItems();
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.addAll(Constants.getMenuCategories());
        
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterMenuItems(position == 0 ? null : categories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterMenuItems(null);
            }
        });
    }

    private void loadMenuItems() {
        progressBar.setVisibility(View.VISIBLE);
        
        menuItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuItems.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MenuItem menuItem = snapshot.getValue(MenuItem.class);
                    if (menuItem != null && menuItem.isAvailability()) {
                        menuItems.add(menuItem);
                    }
                }
                
                // Apply current filter
                filterMenuItems(categorySpinner.getSelectedItemPosition() == 0 ? 
                               null : categorySpinner.getSelectedItem().toString());
                
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MenuBrowseActivity.this, 
                              "Error loading menu items: " + databaseError.getMessage(), 
                              Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterMenuItems(String category) {
        filteredMenuItems.clear();
        
        if (category == null || category.isEmpty()) {
            filteredMenuItems.addAll(menuItems);
        } else {
            for (MenuItem item : menuItems) {
                if (category.equals(item.getCategory())) {
                    filteredMenuItems.add(item);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show empty state if no items
        if (filteredMenuItems.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        // Show menu item details
        showMenuItemDetailsDialog(menuItem);
    }
    
    private void showMenuItemDetailsDialog(MenuItem menuItem) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_menu_item_details, null);
        dialog.setContentView(dialogView);
        
        // Initialize dialog elements
        TextView nameTextView = dialogView.findViewById(R.id.tv_item_name);
        TextView descriptionTextView = dialogView.findViewById(R.id.tv_item_description);
        TextView priceTextView = dialogView.findViewById(R.id.tv_item_price);
        TextView categoryTextView = dialogView.findViewById(R.id.tv_item_category);
        Button closeButton = dialogView.findViewById(R.id.btn_close);
        
        // Set dialog data
        nameTextView.setText(menuItem.getName());
        descriptionTextView.setText(menuItem.getDescription());
        priceTextView.setText(menuItem.getFormattedPrice());
        categoryTextView.setText("Category: " + menuItem.getCategory());
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_refresh) {
            loadMenuItems();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
