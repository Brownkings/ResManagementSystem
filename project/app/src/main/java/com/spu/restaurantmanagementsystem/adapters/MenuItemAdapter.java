package com.spu.restaurantmanagementsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.models.MenuItem;

import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder> {

    private List<MenuItem> menuItems;
    private Context context;
    private OnMenuItemClickListener listener;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem menuItem);
    }

    public MenuItemAdapter(Context context, List<MenuItem> menuItems, OnMenuItemClickListener listener) {
        this.context = context;
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        
        holder.nameTextView.setText(menuItem.getName());
        holder.descriptionTextView.setText(menuItem.getDescription());
        holder.priceTextView.setText(menuItem.getFormattedPrice());
        
        // Using availability to show different status
        if (menuItem.isAvailability()) {
            holder.availabilityTextView.setText("Available");
            holder.availabilityTextView.setTextColor(context.getResources().getColor(R.color.colorAvailable));
        } else {
            holder.availabilityTextView.setText("Unavailable");
            holder.availabilityTextView.setTextColor(context.getResources().getColor(R.color.colorUnavailable));
        }
        
        // Load image using Glide if URL exists
        if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(menuItem.getImageUrl())
                .placeholder(R.drawable.ic_food_placeholder)
                .error(R.drawable.ic_food_placeholder)
                .into(holder.itemImageView);
        } else {
            holder.itemImageView.setImageResource(R.drawable.ic_food_placeholder);
        }
        
        // Set category
        holder.categoryTextView.setText(menuItem.getCategory());
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuItemClick(menuItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }
    
    public void updateMenuItems(List<MenuItem> newMenuItems) {
        this.menuItems = newMenuItems;
        notifyDataSetChanged();
    }

    public static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView nameTextView;
        TextView descriptionTextView;
        TextView priceTextView;
        TextView availabilityTextView;
        TextView categoryTextView;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.iv_menu_item);
            nameTextView = itemView.findViewById(R.id.tv_menu_item_name);
            descriptionTextView = itemView.findViewById(R.id.tv_menu_item_description);
            priceTextView = itemView.findViewById(R.id.tv_menu_item_price);
            availabilityTextView = itemView.findViewById(R.id.tv_menu_item_availability);
            categoryTextView = itemView.findViewById(R.id.tv_menu_item_category);
        }
    }
}
