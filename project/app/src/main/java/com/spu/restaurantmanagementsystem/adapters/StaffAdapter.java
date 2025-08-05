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
import com.spu.restaurantmanagementsystem.models.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private List<User> staffList;
    private Context context;
    private OnStaffClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnStaffClickListener {
        void onStaffClick(User staff);
        void onEditRoleClick(User staff);
        void onRemoveStaffClick(User staff);
    }

    public StaffAdapter(Context context, List<User> staffList, OnStaffClickListener listener) {
        this.context = context;
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        User staff = staffList.get(position);
        
        holder.nameTextView.setText(staff.getName());
        holder.emailTextView.setText(staff.getEmail());
        holder.roleTextView.setText("Role: " + capitalizeRole(staff.getRole()));
        
        if (staff.getCreatedAt() != null) {
            holder.createdAtTextView.setText("Joined: " + dateFormat.format(staff.getCreatedAt()));
        } else {
            holder.createdAtTextView.setText("Joined: Unknown");
        }
        
        // Set click listeners for buttons
        holder.editRoleButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditRoleClick(staff);
            }
        });
        
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveStaffClick(staff);
            }
        });
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStaffClick(staff);
            }
        });
    }

    private String capitalizeRole(String role) {
        if (role == null || role.isEmpty()) {
            return "Unknown";
        }
        return role.substring(0, 1).toUpperCase() + role.substring(1);
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }
    
    public void updateStaffList(List<User> newStaffList) {
        this.staffList = newStaffList;
        notifyDataSetChanged();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView roleTextView;
        TextView createdAtTextView;
        Button editRoleButton;
        Button removeButton;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_staff_name);
            emailTextView = itemView.findViewById(R.id.tv_staff_email);
            roleTextView = itemView.findViewById(R.id.tv_staff_role);
            createdAtTextView = itemView.findViewById(R.id.tv_staff_joined_date);
            editRoleButton = itemView.findViewById(R.id.btn_edit_role);
            removeButton = itemView.findViewById(R.id.btn_remove_staff);
        }
    }
}
