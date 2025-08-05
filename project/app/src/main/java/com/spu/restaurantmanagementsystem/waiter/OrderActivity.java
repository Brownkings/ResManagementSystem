package com.spu.restaurantmanagementsystem.waiter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.adapters.MenuItemAdapter;
import com.spu.restaurantmanagementsystem.adapters.OrderAdapter;
import com.spu.restaurantmanagementsystem.adapters.OrderItemAdapter;
import com.spu.restaurantmanagementsystem.models.MenuItem;
import com.spu.restaurantmanagementsystem.models.Order;
import com.spu.restaurantmanagementsystem.models.OrderItem;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderActivity extends AppCompatActivity implements 
        OrderAdapter.OnOrderClickListener, 
        MenuItemAdapter.OnMenuItemClickListener, 
        OrderItemAdapter.OnOrderItemActionListener {

    private RecyclerView ordersRecyclerView, currentOrderItemsRecyclerView;
    private TextView titleTextView, emptyStateTextView, tableNumberTextView, totalAmountTextView, statusTextView;
    private Spinner tableSpinner;
    private EditText specialNotesEditText;
    private Button createOrderButton, addItemButton, cancelButton, submitOrderButton;
    private FloatingActionButton newOrderFab;
    private ProgressBar progressBar;
    
    private OrderAdapter orderAdapter;
    private OrderItemAdapter orderItemAdapter;
    private List<Order> orderList;
    private List<OrderItem> currentOrderItems;
    
    private String userId;
    private String userName;
    private boolean isNewOrder;
    private Order currentOrder;
    
    private DatabaseReference ordersRef;
    private DatabaseReference orderItemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        
        // Get intent extras
        isNewOrder = getIntent().getBooleanExtra("is_new_order", false);
        
        // Set up toolbar
        getSupportActionBar().setTitle(isNewOrder ? "Create New Order" : "My Orders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase references
        ordersRef = FirebaseUtil.getOrdersRef();
        orderItemsRef = FirebaseUtil.getOrderItemsRef();
        
        // Get user info from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getString(Constants.PREF_USER_ID, null);
        userName = preferences.getString(Constants.PREF_USER_NAME, "Waiter");
        
        // Initialize UI elements
        ordersRecyclerView = findViewById(R.id.rv_orders);
        currentOrderItemsRecyclerView = findViewById(R.id.rv_current_order_items);
        titleTextView = findViewById(R.id.tv_title);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        tableNumberTextView = findViewById(R.id.tv_table_number);
        totalAmountTextView = findViewById(R.id.tv_total_amount);
        statusTextView = findViewById(R.id.tv_status);
        tableSpinner = findViewById(R.id.spinner_table);
        specialNotesEditText = findViewById(R.id.et_special_notes);
        createOrderButton = findViewById(R.id.btn_create_order);
        addItemButton = findViewById(R.id.btn_add_item);
        cancelButton = findViewById(R.id.btn_cancel);
        submitOrderButton = findViewById(R.id.btn_submit_order);
        newOrderFab = findViewById(R.id.fab_new_order);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set up UI based on mode
        setupUIForMode();
        
        // Load data
        if (isNewOrder) {
            setupNewOrderCreation();
        } else {
            loadWaiterOrders();
        }
    }

    private void setupUIForMode() {
        if (isNewOrder) {
            // Creating a new order
            titleTextView.setText("Create New Order");
            ordersRecyclerView.setVisibility(View.GONE);
            newOrderFab.setVisibility(View.GONE);
            
            findViewById(R.id.layout_new_order).setVisibility(View.VISIBLE);
            tableSpinner.setVisibility(View.VISIBLE);
            
            // Set up table spinner
            setupTableSpinner();
            
            // Setup new order creation mode
            createOrderButton.setOnClickListener(v -> {
                // Validate and create a new order
                if (validateNewOrderInputs()) {
                    createNewOrder();
                }
            });
            
            // Initially hide other buttons until order is created
            addItemButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            submitOrderButton.setVisibility(View.GONE);
            currentOrderItemsRecyclerView.setVisibility(View.GONE);
        } else {
            // Viewing existing orders
            titleTextView.setText("My Orders");
            ordersRecyclerView.setVisibility(View.VISIBLE);
            newOrderFab.setVisibility(View.VISIBLE);
            
            findViewById(R.id.layout_new_order).setVisibility(View.GONE);
            
            // Set up RecyclerView for orders
            orderList = new ArrayList<>();
            orderAdapter = new OrderAdapter(this, orderList, this, Constants.ROLE_WAITER);
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            ordersRecyclerView.setAdapter(orderAdapter);
            
            // Set up FAB for creating a new order
            newOrderFab.setOnClickListener(v -> {
                Intent intent = new Intent(OrderActivity.this, OrderActivity.class);
                intent.putExtra("is_new_order", true);
                startActivity(intent);
            });
        }
    }

    private void setupTableSpinner() {
        List<String> tableOptions = new ArrayList<>();
        for (int i = Constants.MIN_TABLE_NUMBER; i <= Constants.MAX_TABLE_NUMBER; i++) {
            tableOptions.add("Table " + i);
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tableOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableSpinner.setAdapter(adapter);
    }

    private boolean validateNewOrderInputs() {
        // Simple validation for now
        return true; // We just need a table number which is always selected in spinner
    }

    private void createNewOrder() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get selected table number
        int tableNumber = tableSpinner.getSelectedItemPosition() + Constants.MIN_TABLE_NUMBER;
        
        // Generate order ID
        String orderId = FirebaseUtil.generateKey(ordersRef);
        
        // Create order object
        currentOrder = new Order(
                orderId,
                tableNumber,
                userId,
                userName,
                new Date(),
                Order.STATUS_RECEIVED,
                specialNotesEditText.getText().toString().trim()
        );
        
        // Initialize order items list
        currentOrderItems = new ArrayList<>();
        
        // Update UI for adding items
        updateUIForAddingItems();
    }

    private void updateUIForAddingItems() {
        // Hide creation UI
        tableSpinner.setVisibility(View.GONE);
        createOrderButton.setVisibility(View.GONE);
        
        // Show order management UI
        tableNumberTextView.setVisibility(View.VISIBLE);
        tableNumberTextView.setText("Table: " + currentOrder.getTableNumber());
        
        statusTextView.setVisibility(View.VISIBLE);
        statusTextView.setText("Status: Preparing Order");
        
        totalAmountTextView.setVisibility(View.VISIBLE);
        totalAmountTextView.setText("Total: R0.00");
        
        addItemButton.setVisibility(View.VISIBLE);
        addItemButton.setOnClickListener(v -> showMenuItemsDialog());
        
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setOnClickListener(v -> {
            // Confirm cancel
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Order")
                    .setMessage("Are you sure you want to cancel this order?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        });
        
        submitOrderButton.setVisibility(View.VISIBLE);
        submitOrderButton.setOnClickListener(v -> {
            // Validate and submit order
            if (validateOrderSubmission()) {
                submitOrder();
            }
        });
        
        // Set up RecyclerView for order items
        currentOrderItemsRecyclerView.setVisibility(View.VISIBLE);
        orderItemAdapter = new OrderItemAdapter(this, currentOrderItems, this, true);
        currentOrderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        currentOrderItemsRecyclerView.setAdapter(orderItemAdapter);
        
        progressBar.setVisibility(View.GONE);
    }

    private boolean validateOrderSubmission() {
        if (currentOrderItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one item to the order", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void submitOrder() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Set order items and recalculate total
        currentOrder.setOrderItems(currentOrderItems);
        
        // Save order to Firebase
        ordersRef.child(currentOrder.getOrderId()).setValue(currentOrder)
                .addOnSuccessListener(aVoid -> {
                    // Now save each order item
                    saveOrderItems();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrderActivity.this, 
                            "Failed to create order: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveOrderItems() {
        int totalItems = currentOrderItems.size();
        final int[] savedItems = {0};
        
        if (totalItems == 0) {
            // No items to save, finish up
            progressBar.setVisibility(View.GONE);
            showOrderSubmittedDialog();
            return;
        }
        
        for (OrderItem item : currentOrderItems) {
            orderItemsRef.child(item.getOrderItemId()).setValue(item)
                    .addOnSuccessListener(aVoid -> {
                        savedItems[0]++;
                        if (savedItems[0] >= totalItems) {
                            // All items saved
                            progressBar.setVisibility(View.GONE);
                            showOrderSubmittedDialog();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OrderActivity.this, 
                                "Error saving order items: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showOrderSubmittedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Order Submitted")
                .setMessage("Order has been successfully submitted to the kitchen.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Go back to previous screen
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showMenuItemsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_menu_selection, null);
        dialog.setContentView(dialogView);
        
        RecyclerView menuRecyclerView = dialogView.findViewById(R.id.rv_menu_items);
        ProgressBar dialogProgressBar = dialogView.findViewById(R.id.progress_bar);
        TextView emptyTextView = dialogView.findViewById(R.id.tv_no_items);
        
        // Load menu items
        dialogProgressBar.setVisibility(View.VISIBLE);
        
        FirebaseUtil.getMenuItemsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<MenuItem> menuItems = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MenuItem menuItem = snapshot.getValue(MenuItem.class);
                    if (menuItem != null && menuItem.isAvailability()) {
                        menuItems.add(menuItem);
                    }
                }
                
                dialogProgressBar.setVisibility(View.GONE);
                
                if (menuItems.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    // Set up RecyclerView
                    MenuItemAdapter adapter = new MenuItemAdapter(OrderActivity.this, menuItems, 
                            menuItem -> {
                                // Add item to order
                                addMenuItemToOrder(menuItem);
                                dialog.dismiss();
                            });
                    
                    menuRecyclerView.setLayoutManager(new LinearLayoutManager(OrderActivity.this));
                    menuRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialogProgressBar.setVisibility(View.GONE);
                Toast.makeText(OrderActivity.this, 
                        "Error loading menu: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }

    private void addMenuItemToOrder(MenuItem menuItem) {
        // Show quantity dialog
        showQuantityAndNotesDialog(menuItem);
    }

    private void showQuantityAndNotesDialog(MenuItem menuItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_order_item, null);
        builder.setView(dialogView);
        
        TextView nameTextView = dialogView.findViewById(R.id.tv_item_name);
        TextView priceTextView = dialogView.findViewById(R.id.tv_item_price);
        EditText quantityEditText = dialogView.findViewById(R.id.et_quantity);
        EditText notesEditText = dialogView.findViewById(R.id.et_notes);
        Button addButton = dialogView.findViewById(R.id.btn_add);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        
        nameTextView.setText(menuItem.getName());
        priceTextView.setText(menuItem.getFormattedPrice());
        quantityEditText.setText("1");
        
        AlertDialog dialog = builder.create();
        
        addButton.setOnClickListener(v -> {
            // Validate quantity
            String quantityStr = quantityEditText.getText().toString().trim();
            if (quantityStr.isEmpty()) {
                quantityEditText.setError("Required");
                return;
            }
            
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    quantityEditText.setError("Must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                quantityEditText.setError("Invalid number");
                return;
            }
            
            // Create order item
            String orderItemId = FirebaseUtil.generateKey(orderItemsRef);
            OrderItem orderItem = new OrderItem(
                    orderItemId,
                    currentOrder.getOrderId(),
                    menuItem.getItemId(),
                    menuItem.getName(),
                    quantity,
                    menuItem.getPrice(),
                    notesEditText.getText().toString().trim()
            );
            
            // Add to current order items
            currentOrderItems.add(orderItem);
            orderItemAdapter.notifyDataSetChanged();
            
            // Update total
            updateOrderTotal();
            
            dialog.dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void updateOrderTotal() {
        double total = 0;
        for (OrderItem item : currentOrderItems) {
            total += item.getSubtotal();
        }
        
        currentOrder.setTotalAmount(total);
        totalAmountTextView.setText("Total: " + currentOrder.getFormattedTotal());
    }

    private void loadWaiterOrders() {
        progressBar.setVisibility(View.VISIBLE);
        
        ordersRef.orderByChild("waiterId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderList.clear();
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }
                        
                        orderAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        
                        // Show/hide empty state
                        if (orderList.isEmpty()) {
                            emptyStateTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyStateTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OrderActivity.this, 
                                "Error loading orders: " + databaseError.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onOrderClick(Order order) {
        // Show order details
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onStatusUpdateClick(Order order, String newStatus) {
        // Update order status
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseUtil.updateOrderStatus(order.getOrderId(), newStatus, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(OrderActivity.this, 
                        "Order status updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OrderActivity.this, 
                        "Failed to update status: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        // Already handled in dialog
    }

    @Override
    public void onIncreaseQuantity(OrderItem orderItem, int position) {
        orderItem.setQuantity(orderItem.getQuantity() + 1);
        orderItemAdapter.notifyItemChanged(position);
        updateOrderTotal();
    }

    @Override
    public void onDecreaseQuantity(OrderItem orderItem, int position) {
        int newQuantity = orderItem.getQuantity() - 1;
        if (newQuantity >= 1) {
            orderItem.setQuantity(newQuantity);
            orderItemAdapter.notifyItemChanged(position);
            updateOrderTotal();
        }
    }

    @Override
    public void onRemoveItem(OrderItem orderItem, int position) {
        currentOrderItems.remove(position);
        orderItemAdapter.notifyDataSetChanged();
        updateOrderTotal();
    }

    // Order details activity for when a waiter clicks on an existing order
    public static class OrderDetailsActivity extends AppCompatActivity {
        
        private TextView orderIdTextView, tableNumberTextView, statusTextView, totalTextView;
        private RecyclerView orderItemsRecyclerView;
        private Button statusButton;
        private ProgressBar progressBar;
        
        private String orderId;
        private Order currentOrder;
        private List<OrderItem> orderItems;
        private OrderItemAdapter adapter;
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_order_details);
            
            // Get order ID from intent
            orderId = getIntent().getStringExtra("order_id");
            if (orderId == null) {
                Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Set up toolbar
            getSupportActionBar().setTitle("Order Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            
            // Initialize UI elements
            orderIdTextView = findViewById(R.id.tv_order_id);
            tableNumberTextView = findViewById(R.id.tv_table_number);
            statusTextView = findViewById(R.id.tv_status);
            totalTextView = findViewById(R.id.tv_total);
            orderItemsRecyclerView = findViewById(R.id.rv_order_items);
            statusButton = findViewById(R.id.btn_update_status);
            progressBar = findViewById(R.id.progress_bar);
            
            // Set up RecyclerView
            orderItems = new ArrayList<>();
            adapter = new OrderItemAdapter(this, orderItems, null, false);
            orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            orderItemsRecyclerView.setAdapter(adapter);
            
            // Load order details
            loadOrderDetails();
        }
        
        private void loadOrderDetails() {
            progressBar.setVisibility(View.VISIBLE);
            
            // Load order
            FirebaseUtil.getOrdersRef().child(orderId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    currentOrder = dataSnapshot.getValue(Order.class);
                    if (currentOrder == null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OrderDetailsActivity.this, 
                                "Order not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    
                    // Update UI with order details
                    updateOrderUI();
                    
                    // Load order items
                    loadOrderItems();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrderDetailsActivity.this, 
                            "Error loading order: " + databaseError.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        private void updateOrderUI() {
            orderIdTextView.setText("Order #" + currentOrder.getOrderId());
            tableNumberTextView.setText("Table: " + currentOrder.getTableNumber());
            statusTextView.setText("Status: " + getReadableStatus(currentOrder.getStatus()));
            totalTextView.setText("Total: " + currentOrder.getFormattedTotal());
            
            // Update status button based on current status
            if (currentOrder.isReady()) {
                statusButton.setVisibility(View.VISIBLE);
                statusButton.setText("Mark as Served");
                statusButton.setOnClickListener(v -> updateOrderStatus(Order.STATUS_SERVED));
            } else {
                statusButton.setVisibility(View.GONE);
            }
        }
        
        private void loadOrderItems() {
            FirebaseUtil.getOrderItemsRef().orderByChild("orderId").equalTo(orderId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            orderItems.clear();
                            
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                OrderItem item = snapshot.getValue(OrderItem.class);
                                if (item != null) {
                                    orderItems.add(item);
                                }
                            }
                            
                            adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(OrderDetailsActivity.this, 
                                    "Error loading items: " + databaseError.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        
        private void updateOrderStatus(String newStatus) {
            progressBar.setVisibility(View.VISIBLE);
            
            FirebaseUtil.updateOrderStatus(orderId, newStatus, task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(OrderDetailsActivity.this, 
                            "Order status updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrderDetailsActivity.this, 
                            "Failed to update status: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        private String getReadableStatus(String status) {
            switch (status) {
                case Order.STATUS_RECEIVED:
                    return "Received";
                case Order.STATUS_PREPARING:
                    return "Preparing";
                case Order.STATUS_READY:
                    return "Ready";
                case Order.STATUS_SERVED:
                    return "Served";
                default:
                    return status;
            }
        }
        
        @Override
        public boolean onSupportNavigateUp() {
            onBackPressed();
            return true;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
