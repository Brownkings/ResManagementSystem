package com.spu.restaurantmanagementsystem.chef;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.adapters.OrderAdapter;
import com.spu.restaurantmanagementsystem.adapters.OrderItemAdapter;
import com.spu.restaurantmanagementsystem.models.Order;
import com.spu.restaurantmanagementsystem.models.OrderItem;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderViewActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private RecyclerView ordersRecyclerView;
    private TextView titleTextView, emptyStateTextView;
    private ProgressBar progressBar;
    
    private OrderAdapter adapter;
    private List<Order> orderList;
    
    private String orderStatus;
    private DatabaseReference ordersRef;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);
        
        // Get order status from intent
        orderStatus = getIntent().getStringExtra("order_status");
        if (orderStatus == null) {
            orderStatus = Order.STATUS_RECEIVED; // Default to received orders
        }
        
        // Set up toolbar
        String title;
        switch (orderStatus) {
            case Order.STATUS_RECEIVED:
                title = "Pending Orders";
                break;
            case Order.STATUS_PREPARING:
                title = "Orders in Preparation";
                break;
            case Order.STATUS_READY:
                title = "Ready Orders";
                break;
            default:
                title = "Orders";
                break;
        }
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        ordersRef = FirebaseUtil.getOrdersRef();
        
        // Initialize UI elements
        ordersRecyclerView = findViewById(R.id.rv_orders);
        titleTextView = findViewById(R.id.tv_title);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set title
        titleTextView.setText(title);
        
        // Set up RecyclerView
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(this, orderList, this, "chef");
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);
        
        // Load orders
        loadOrders();
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        
        ordersRef.orderByChild("status").equalTo(orderStatus)
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
                        
                        adapter.notifyDataSetChanged();
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
                        Toast.makeText(OrderViewActivity.this, 
                                "Error loading orders: " + databaseError.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onOrderClick(Order order) {
        // Show order details
        showOrderDetailsDialog(order);
    }

    @Override
    public void onStatusUpdateClick(Order order, String newStatus) {
        // Update order status
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseUtil.updateOrderStatus(order.getOrderId(), newStatus, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(OrderViewActivity.this, 
                        "Order status updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OrderViewActivity.this, 
                        "Failed to update status: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showOrderDetailsDialog(Order order) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_details, null);
        dialog.setContentView(dialogView);
        
        // Initialize dialog elements
        TextView orderIdTextView = dialogView.findViewById(R.id.tv_order_id);
        TextView tableNumberTextView = dialogView.findViewById(R.id.tv_table_number);
        TextView timeTextView = dialogView.findViewById(R.id.tv_time);
        TextView waiterNameTextView = dialogView.findViewById(R.id.tv_waiter_name);
        TextView statusTextView = dialogView.findViewById(R.id.tv_status);
        TextView notesTextView = dialogView.findViewById(R.id.tv_notes);
        RecyclerView itemsRecyclerView = dialogView.findViewById(R.id.rv_order_items);
        
        // Set order details
        orderIdTextView.setText("Order #" + order.getOrderId());
        tableNumberTextView.setText("Table: " + order.getTableNumber());
        timeTextView.setText("Time: " + dateFormat.format(order.getOrderTime()));
        waiterNameTextView.setText("Waiter: " + order.getWaiterName());
        statusTextView.setText("Status: " + getReadableStatus(order.getStatus()));
        
        if (order.getSpecialNotes() != null && !order.getSpecialNotes().isEmpty()) {
            notesTextView.setText("Notes: " + order.getSpecialNotes());
            notesTextView.setVisibility(View.VISIBLE);
        } else {
            notesTextView.setVisibility(View.GONE);
        }
        
        // Load order items
        loadOrderItemsForDialog(order.getOrderId(), itemsRecyclerView);
        
        dialog.show();
    }
    
    private void loadOrderItemsForDialog(String orderId, RecyclerView recyclerView) {
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItemAdapter itemsAdapter = new OrderItemAdapter(this, orderItems, null, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemsAdapter);
        
        // Fetch order items
        FirebaseUtil.getOrderItemsRef().orderByChild("orderId").equalTo(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderItems.clear();
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            OrderItem item = snapshot.getValue(OrderItem.class);
                            if (item != null) {
                                orderItems.add(item);
                            }
                        }
                        
                        itemsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(OrderViewActivity.this, 
                                "Error loading order items: " + databaseError.getMessage(), 
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
