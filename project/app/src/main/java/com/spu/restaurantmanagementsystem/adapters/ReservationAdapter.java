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
import com.spu.restaurantmanagementsystem.models.Reservation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservations;
    private Context context;
    private OnReservationClickListener listener;
    private String userRole;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnReservationClickListener {
        void onReservationClick(Reservation reservation);
        void onStatusUpdateClick(Reservation reservation, String newStatus);
    }

    public ReservationAdapter(Context context, List<Reservation> reservations, OnReservationClickListener listener, String userRole) {
        this.context = context;
        this.reservations = reservations;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        
        holder.idTextView.setText("Reservation #" + reservation.getReservationId());
        holder.dateTextView.setText(dateFormat.format(reservation.getReservationDate()));
        holder.timeTextView.setText("Time: " + reservation.getReservationTime());
        holder.guestsTextView.setText("Guests: " + reservation.getNumberOfGuests());
        holder.statusTextView.setText("Status: " + getReadableStatus(reservation.getStatus()));
        
        // For customers, show cancel button for pending or confirmed reservations
        if ("customer".equals(userRole)) {
            if (reservation.isPending() || reservation.isConfirmed()) {
                holder.actionButton1.setVisibility(View.VISIBLE);
                holder.actionButton1.setText("Cancel Reservation");
                holder.actionButton1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusUpdateClick(reservation, Reservation.STATUS_CANCELLED);
                    }
                });
            } else {
                holder.actionButton1.setVisibility(View.GONE);
            }
            holder.actionButton2.setVisibility(View.GONE);
        } 
        // For managers, show confirm/cancel buttons for pending reservations
        else if ("manager".equals(userRole)) {
            if (reservation.isPending()) {
                holder.actionButton1.setVisibility(View.VISIBLE);
                holder.actionButton1.setText("Confirm");
                holder.actionButton1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusUpdateClick(reservation, Reservation.STATUS_CONFIRMED);
                    }
                });
                
                holder.actionButton2.setVisibility(View.VISIBLE);
                holder.actionButton2.setText("Cancel");
                holder.actionButton2.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusUpdateClick(reservation, Reservation.STATUS_CANCELLED);
                    }
                });
            } else if (reservation.isConfirmed()) {
                holder.actionButton1.setVisibility(View.VISIBLE);
                holder.actionButton1.setText("Cancel");
                holder.actionButton1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusUpdateClick(reservation, Reservation.STATUS_CANCELLED);
                    }
                });
                holder.actionButton2.setVisibility(View.GONE);
            } else {
                holder.actionButton1.setVisibility(View.GONE);
                holder.actionButton2.setVisibility(View.GONE);
            }
        } else {
            // For waiters or chefs, no action buttons
            holder.actionButton1.setVisibility(View.GONE);
            holder.actionButton2.setVisibility(View.GONE);
        }
        
        // Add customer name for managers/waiters
        if ("manager".equals(userRole) || "waiter".equals(userRole)) {
            holder.customerNameTextView.setVisibility(View.VISIBLE);
            holder.customerNameTextView.setText("Customer: " + reservation.getCustomerName());
        } else {
            holder.customerNameTextView.setVisibility(View.GONE);
        }
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservationClick(reservation);
            }
        });
    }

    private String getReadableStatus(String status) {
        switch (status) {
            case Reservation.STATUS_PENDING:
                return "Pending";
            case Reservation.STATUS_CONFIRMED:
                return "Confirmed";
            case Reservation.STATUS_CANCELLED:
                return "Cancelled";
            default:
                return status;
        }
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }
    
    public void updateReservations(List<Reservation> newReservations) {
        this.reservations = newReservations;
        notifyDataSetChanged();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView idTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView guestsTextView;
        TextView statusTextView;
        TextView customerNameTextView;
        Button actionButton1;
        Button actionButton2;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.tv_reservation_id);
            dateTextView = itemView.findViewById(R.id.tv_reservation_date);
            timeTextView = itemView.findViewById(R.id.tv_reservation_time);
            guestsTextView = itemView.findViewById(R.id.tv_reservation_guests);
            statusTextView = itemView.findViewById(R.id.tv_reservation_status);
            customerNameTextView = itemView.findViewById(R.id.tv_customer_name);
            actionButton1 = itemView.findViewById(R.id.btn_action_1);
            actionButton2 = itemView.findViewById(R.id.btn_action_2);
        }
    }
}
