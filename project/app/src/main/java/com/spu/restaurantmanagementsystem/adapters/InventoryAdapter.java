package com.spu.restaurantmanagementsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.models.InventoryItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> inventoryItems;
    private Context context;
    private OnInventoryItemClickListener listener;
    private boolean isEditable;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public interface OnInventoryItemClickListener {
        void onInventoryItemClick(InventoryItem inventoryItem);
        void onEditItemClick(InventoryItem inventoryItem);
    }

    public InventoryAdapter(Context context, List<InventoryItem> inventoryItems, 
                           OnInventoryItemClickListener listener, boolean isEditable) {
        this.context = context;
        this.inventoryItems = inventoryItems;
        this.listener = listener;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryItems.get(position);
        
        holder.nameTextView.setText(item.getItemName());
        holder.quantityTextView.setText(item.getFormattedQuantity());
        holder.costTextView.setText(item.getFormattedCost());
        holder.lastUpdatedTextView.setText("Last updated: " + dateFormat.format(item.getLastUpdated()));
        
        // Set stock status with color indicator
        holder.stockStatusTextView.setText(item.getStockStatus());
        if (item.isLowStock()) {
            holder.stockStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.colorLowStock));
        } else {
            holder.stockStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAvailable));
        }
        
        // Show total value
        holder.valueTextView.setText("Value: " + item.getFormattedValue());
        
        // Show/hide edit button based on editability
        if (isEditable) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditItemClick(item);
                }
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
        }
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInventoryItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }
    
    public void updateInventoryItems(List<InventoryItem> newItems) {
        this.inventoryItems = newItems;
        notifyDataSetChanged();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView quantityTextView;
        TextView costTextView;
        TextView lastUpdatedTextView;
        TextView stockStatusTextView;
        TextView valueTextView;
        Button editButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_inventory_name);
            quantityTextView = itemView.findViewById(R.id.tv_inventory_quantity);
            costTextView = itemView.findViewById(R.id.tv_inventory_cost);
            lastUpdatedTextView = itemView.findViewById(R.id.tv_last_updated);
            stockStatusTextView = itemView.findViewById(R.id.tv_stock_status);
            valueTextView = itemView.findViewById(R.id.tv_inventory_value);
            editButton = itemView.findViewById(R.id.btn_edit_inventory);
        }
    }
}
