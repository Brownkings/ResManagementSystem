package com.spu.restaurantmanagementsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.models.OrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> orderItems;
    private Context context;
    private OnOrderItemActionListener listener;
    private boolean isEditable;

    public interface OnOrderItemActionListener {
        void onIncreaseQuantity(OrderItem orderItem, int position);
        void onDecreaseQuantity(OrderItem orderItem, int position);
        void onRemoveItem(OrderItem orderItem, int position);
    }

    public OrderItemAdapter(Context context, List<OrderItem> orderItems, OnOrderItemActionListener listener, boolean isEditable) {
        this.context = context;
        this.orderItems = orderItems;
        this.listener = listener;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);
        
        holder.nameTextView.setText(orderItem.getItemName());
        holder.priceTextView.setText(orderItem.getFormattedPrice());
        holder.quantityTextView.setText(String.valueOf(orderItem.getQuantity()));
        holder.subtotalTextView.setText(orderItem.getFormattedSubtotal());
        
        if (orderItem.getNotes() != null && !orderItem.getNotes().isEmpty()) {
            holder.notesTextView.setVisibility(View.VISIBLE);
            holder.notesTextView.setText("Note: " + orderItem.getNotes());
        } else {
            holder.notesTextView.setVisibility(View.GONE);
        }
        
        // Show/hide quantity control buttons based on editability
        if (isEditable) {
            holder.increaseButton.setVisibility(View.VISIBLE);
            holder.decreaseButton.setVisibility(View.VISIBLE);
            holder.removeButton.setVisibility(View.VISIBLE);
            
            holder.increaseButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncreaseQuantity(orderItem, position);
                }
            });
            
            holder.decreaseButton.setOnClickListener(v -> {
                if (listener != null && orderItem.getQuantity() > 1) {
                    listener.onDecreaseQuantity(orderItem, position);
                }
            });
            
            holder.removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(orderItem, position);
                }
            });
            
            // Disable decrease button if quantity is 1
            holder.decreaseButton.setEnabled(orderItem.getQuantity() > 1);
        } else {
            holder.increaseButton.setVisibility(View.GONE);
            holder.decreaseButton.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }
    
    public void updateOrderItems(List<OrderItem> newOrderItems) {
        this.orderItems = newOrderItems;
        notifyDataSetChanged();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView priceTextView;
        TextView quantityTextView;
        TextView subtotalTextView;
        TextView notesTextView;
        ImageButton increaseButton;
        ImageButton decreaseButton;
        ImageButton removeButton;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_item_name);
            priceTextView = itemView.findViewById(R.id.tv_item_price);
            quantityTextView = itemView.findViewById(R.id.tv_item_quantity);
            subtotalTextView = itemView.findViewById(R.id.tv_item_subtotal);
            notesTextView = itemView.findViewById(R.id.tv_item_notes);
            increaseButton = itemView.findViewById(R.id.btn_increase_quantity);
            decreaseButton = itemView.findViewById(R.id.btn_decrease_quantity);
            removeButton = itemView.findViewById(R.id.btn_remove_item);
        }
    }
}
