package com.spu.restaurantmanagementsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.models.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private Context context;
    private OnOrderClickListener listener;
    private String userRole;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onStatusUpdateClick(Order order, String newStatus);
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener, String userRole) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        
        holder.orderIdTextView.setText("Order #" + order.getOrderId());
        holder.tableNumberTextView.setText("Table: " + order.getTableNumber());
        holder.statusTextView.setText("Status: " + getReadableStatus(order.getStatus()));
        holder.timeTextView.setText(dateFormat.format(order.getOrderTime()));
        holder.totalTextView.setText("Total: " + order.getFormattedTotal());
        
        // Show/hide action buttons based on role and order status
        if ("waiter".equals(userRole)) {
            setupWaiterButtons(holder, order);
        } else if ("chef".equals(userRole)) {
            setupChefButtons(holder, order);
        } else {
            // Hide all buttons for customers or managers (who manage from order details)
            holder.actionButton1.setVisibility(View.GONE);
            holder.actionButton2.setVisibility(View.GONE);
        }
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    private void setupWaiterButtons(OrderViewHolder holder, Order order) {
        if (order.isReceived()) {
            // Show button to mark order as served when it's ready
            holder.actionButton1.setVisibility(View.GONE);
            holder.actionButton2.setVisibility(View.GONE);
        } else if (order.isReady()) {
            holder.actionButton1.setVisibility(View.VISIBLE);
            holder.actionButton1.setText("Mark as Served");
            holder.actionButton1.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStatusUpdateClick(order, Order.STATUS_SERVED);
                }
            });
            holder.actionButton2.setVisibility(View.GONE);
        } else {
            // For orders in preparing or other states
            holder.actionButton1.setVisibility(View.GONE);
            holder.actionButton2.setVisibility(View.GONE);
        }
    }

    private void setupChefButtons(OrderViewHolder holder, Order order) {
        if (order.isReceived()) {
            holder.actionButton1.setVisibility(View.VISIBLE);
            holder.actionButton1.setText("Start Preparing");
            holder.actionButton1.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStatusUpdateClick(order, Order.STATUS_PREPARING);
                }
            });
            holder.actionButton2.setVisibility(View.GONE);
        } else if (order.isPreparing()) {
            holder.actionButton1.setVisibility(View.VISIBLE);
            holder.actionButton1.setText("Mark as Ready");
            holder.actionButton1.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStatusUpdateClick(order, Order.STATUS_READY);
                }
            });
            holder.actionButton2.setVisibility(View.GONE);
        } else {
            // For ready or served orders
            holder.actionButton1.setVisibility(View.GONE);
            holder.actionButton2.setVisibility(View.GONE);
        }
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
    public int getItemCount() {
        return orders.size();
    }
    
    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView;
        TextView tableNumberTextView;
        TextView statusTextView;
        TextView timeTextView;
        TextView totalTextView;
        Button actionButton1;
        Button actionButton2;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.tv_order_id);
            tableNumberTextView = itemView.findViewById(R.id.tv_table_number);
            statusTextView = itemView.findViewById(R.id.tv_order_status);
            timeTextView = itemView.findViewById(R.id.tv_order_time);
            totalTextView = itemView.findViewById(R.id.tv_order_total);
            actionButton1 = itemView.findViewById(R.id.btn_action_1);
            actionButton2 = itemView.findViewById(R.id.btn_action_2);
        }
    }
}
