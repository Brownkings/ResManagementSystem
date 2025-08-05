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
import com.spu.restaurantmanagementsystem.adapters.StaffAdapter;
import com.spu.restaurantmanagementsystem.models.User;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaffManagementActivity extends AppCompatActivity implements StaffAdapter.OnStaffClickListener {

    private RecyclerView staffRecyclerView;
    private FloatingActionButton addStaffFab;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    
    private StaffAdapter adapter;
    private List<User> staffList;
    
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Staff Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        usersRef = FirebaseUtil.getUsersRef();
        
        // Initialize UI elements
        staffRecyclerView = findViewById(R.id.rv_staff);
        addStaffFab = findViewById(R.id.fab_add_staff);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        
        // Set up RecyclerView
        staffList = new ArrayList<>();
        adapter = new StaffAdapter(this, staffList, this);
        staffRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffRecyclerView.setAdapter(adapter);
        
        // Set up FAB
        addStaffFab.setOnClickListener(v -> showAddStaffDialog());
        
        // Load staff
        loadStaff();
    }

    private void loadStaff() {
        progressBar.setVisibility(View.VISIBLE);
        
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                staffList.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !user.isCustomer()) {
                        staffList.add(user);
                    }
                }
                
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                
                // Show/hide empty state
                if (staffList.isEmpty()) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StaffManagementActivity.this, 
                        "Error loading staff: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStaffClick(User staff) {
        // Show staff details dialog
        showStaffDetailsDialog(staff);
    }

    @Override
    public void onEditRoleClick(User staff) {
        // Show edit role dialog
        showEditRoleDialog(staff);
    }

    @Override
    public void onRemoveStaffClick(User staff) {
        // Show confirm remove dialog
        new AlertDialog.Builder(this)
                .setTitle("Remove Staff")
                .setMessage("Are you sure you want to remove " + staff.getName() + " from the staff?")
                .setPositiveButton("Remove", (dialog, which) -> removeStaff(staff))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showAddStaffDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_staff, null);
        builder.setView(dialogView);
        
        // Initialize dialog elements
        EditText nameEditText = dialogView.findViewById(R.id.et_name);
        EditText emailEditText = dialogView.findViewById(R.id.et_email);
        EditText passwordEditText = dialogView.findViewById(R.id.et_password);
        Spinner roleSpinner = dialogView.findViewById(R.id.spinner_role);
        Button addButton = dialogView.findViewById(R.id.btn_add);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        
        // Set up role spinner
        List<String> roles = Arrays.asList(
                "Chef", 
                "Waiter", 
                "Manager"
        );
        
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        
        builder.setTitle("Add New Staff");
        
        AlertDialog dialog = builder.create();
        
        addButton.setOnClickListener(v -> {
            if (validateStaffInput(nameEditText, emailEditText, passwordEditText)) {
                // Create new staff account
                createStaffAccount(
                        nameEditText.getText().toString().trim(),
                        emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim(),
                        roles.get(roleSpinner.getSelectedItemPosition()).toLowerCase()
                );
                dialog.dismiss();
            }
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private boolean validateStaffInput(EditText nameEditText, EditText emailEditText, EditText passwordEditText) {
        boolean isValid = true;
        
        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameEditText.setError("Name is required");
            isValid = false;
        }
        
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            isValid = false;
        }
        
        String password = passwordEditText.getText().toString().trim();
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            isValid = false;
        }
        
        return isValid;
    }
    
    private void createStaffAccount(String name, String email, String password, String role) {
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseUtil.createNewUser(email, password, name, role, task -> {
            progressBar.setVisibility(View.GONE);
            
            if (task.isSuccessful()) {
                Toast.makeText(StaffManagementActivity.this, 
                        "Staff account created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(StaffManagementActivity.this, 
                        "Failed to create account: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showStaffDetailsDialog(User staff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_staff_details, null);
        builder.setView(dialogView);
        
        // Initialize dialog elements
        TextView nameTextView = dialogView.findViewById(R.id.tv_name);
        TextView emailTextView = dialogView.findViewById(R.id.tv_email);
        TextView roleTextView = dialogView.findViewById(R.id.tv_role);
        TextView createdAtTextView = dialogView.findViewById(R.id.tv_created_at);
        Button editRoleButton = dialogView.findViewById(R.id.btn_edit_role);
        Button removeButton = dialogView.findViewById(R.id.btn_remove);
        Button closeButton = dialogView.findViewById(R.id.btn_close);
        
        // Set dialog data
        nameTextView.setText(staff.getName());
        emailTextView.setText(staff.getEmail());
        roleTextView.setText("Role: " + capitalizeRole(staff.getRole()));
        
        if (staff.getCreatedAt() != null) {
            createdAtTextView.setText("Joined: " + 
                    new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                            .format(staff.getCreatedAt()));
        } else {
            createdAtTextView.setText("Joined: Unknown");
        }
        
        builder.setTitle("Staff Details");
        
        AlertDialog dialog = builder.create();
        
        editRoleButton.setOnClickListener(v -> {
            dialog.dismiss();
            showEditRoleDialog(staff);
        });
        
        removeButton.setOnClickListener(v -> {
            dialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Remove Staff")
                    .setMessage("Are you sure you want to remove " + staff.getName() + " from the staff?")
                    .setPositiveButton("Remove", (confirmDialog, which) -> removeStaff(staff))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void showEditRoleDialog(User staff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_role, null);
        builder.setView(dialogView);
        
        // Initialize dialog elements
        TextView nameTextView = dialogView.findViewById(R.id.tv_name);
        Spinner roleSpinner = dialogView.findViewById(R.id.spinner_role);
        Button saveButton = dialogView.findViewById(R.id.btn_save);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        
        // Set up role spinner
        List<String> roles = Arrays.asList(
                "Chef", 
                "Waiter", 
                "Manager"
        );
        
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        
        // Set dialog data
        nameTextView.setText(staff.getName());
        
        // Set current role
        String currentRole = capitalizeRole(staff.getRole());
        int rolePosition = roles.indexOf(currentRole);
        if (rolePosition >= 0) {
            roleSpinner.setSelection(rolePosition);
        }
        
        builder.setTitle("Edit Staff Role");
        
        AlertDialog dialog = builder.create();
        
        saveButton.setOnClickListener(v -> {
            // Update staff role
            updateStaffRole(staff, roles.get(roleSpinner.getSelectedItemPosition()).toLowerCase());
            dialog.dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void updateStaffRole(User staff, String newRole) {
        progressBar.setVisibility(View.VISIBLE);
        
        usersRef.child(staff.getUserId()).child("role").setValue(newRole)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(StaffManagementActivity.this, 
                            "Staff role updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(StaffManagementActivity.this, 
                            "Failed to update role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void removeStaff(User staff) {
        progressBar.setVisibility(View.VISIBLE);
        
        // In a real app, you would also remove the authentication account
        // Here we just remove from the database
        usersRef.child(staff.getUserId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(StaffManagementActivity.this, 
                            "Staff removed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(StaffManagementActivity.this, 
                            "Failed to remove staff: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private String capitalizeRole(String role) {
        if (role == null || role.isEmpty()) {
            return "Unknown";
        }
        return role.substring(0, 1).toUpperCase() + role.substring(1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
