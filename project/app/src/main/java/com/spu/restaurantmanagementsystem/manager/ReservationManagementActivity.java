package com.spu.restaurantmanagementsystem.manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.adapters.ReservationAdapter;
import com.spu.restaurantmanagementsystem.models.Reservation;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationManagementActivity extends AppCompatActivity implements ReservationAdapter.OnReservationClickListener {

    private RecyclerView reservationsRecyclerView;
    private Button dateSelectorButton, allReservationsButton, pendingButton, confirmedButton, cancelledButton;
    private TextView dateTextView, emptyStateTextView;
    private ProgressBar progressBar;
    
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    
    private DatabaseReference reservationsRef;
    private Date selectedDate;
    private String currentFilter = "all";
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_management);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Reservation Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        reservationsRef = FirebaseUtil.getReservationsRef();
        
        // Initialize UI elements
        reservationsRecyclerView = findViewById(R.id.rv_reservations);
        dateSelectorButton = findViewById(R.id.btn_select_date);
        allReservationsButton = findViewById(R.id.btn_all_reservations);
        pendingButton = findViewById(R.id.btn_pending);
        confirmedButton = findViewById(R.id.btn_confirmed);
        cancelledButton = findViewById(R.id.btn_cancelled);
        dateTextView = findViewById(R.id.tv_selected_date);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set up RecyclerView
        reservationList = new ArrayList<>();
        adapter = new ReservationAdapter(this, reservationList, this, Constants.ROLE_MANAGER);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(adapter);
        
        // Set default date to today
        selectedDate = new Date();
        dateTextView.setText(dateFormat.format(selectedDate));
        
        // Set up button click listeners
        setupClickListeners();
        
        // Load reservations for today by default
        loadReservationsForDate(selectedDate);
    }

    private void setupClickListeners() {
        dateSelectorButton.setOnClickListener(v -> {
            // Show date picker
            Calendar calendar = Calendar.getInstance();
            if (selectedDate != null) {
                calendar.setTime(selectedDate);
            }
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ReservationManagementActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);
                        selectedDate = selectedCalendar.getTime();
                        dateTextView.setText(dateFormat.format(selectedDate));
                        loadReservationsForDate(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        allReservationsButton.setOnClickListener(v -> {
            currentFilter = "all";
            updateButtonStyles();
            filterReservations();
        });
        
        pendingButton.setOnClickListener(v -> {
            currentFilter = Reservation.STATUS_PENDING;
            updateButtonStyles();
            filterReservations();
        });
        
        confirmedButton.setOnClickListener(v -> {
            currentFilter = Reservation.STATUS_CONFIRMED;
            updateButtonStyles();
            filterReservations();
        });
        
        cancelledButton.setOnClickListener(v -> {
            currentFilter = Reservation.STATUS_CANCELLED;
            updateButtonStyles();
            filterReservations();
        });
    }
    
    private void updateButtonStyles() {
        // Reset all buttons
        allReservationsButton.setBackgroundResource(R.drawable.button_outline);
        pendingButton.setBackgroundResource(R.drawable.button_outline);
        confirmedButton.setBackgroundResource(R.drawable.button_outline);
        cancelledButton.setBackgroundResource(R.drawable.button_outline);
        
        // Highlight selected button
        switch (currentFilter) {
            case "all":
                allReservationsButton.setBackgroundResource(R.drawable.button_filled);
                break;
            case Reservation.STATUS_PENDING:
                pendingButton.setBackgroundResource(R.drawable.button_filled);
                break;
            case Reservation.STATUS_CONFIRMED:
                confirmedButton.setBackgroundResource(R.drawable.button_filled);
                break;
            case Reservation.STATUS_CANCELLED:
                cancelledButton.setBackgroundResource(R.drawable.button_filled);
                break;
        }
    }

    private void loadReservationsForDate(Date date) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Clear current list
        reservationList.clear();
        adapter.notifyDataSetChanged();
        
        // Calculate start and end of the selected day
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(date);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(date);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        
        // Query reservations for the selected date
        Query dateQuery = reservationsRef.orderByChild("reservationDate")
                .startAt(startOfDay.getTimeInMillis())
                .endAt(endOfDay.getTimeInMillis());
        
        dateQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationList.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reservation reservation = snapshot.getValue(Reservation.class);
                    if (reservation != null) {
                        reservationList.add(reservation);
                    }
                }
                
                // Apply filter
                filterReservations();
                
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReservationManagementActivity.this, 
                        "Error loading reservations: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void filterReservations() {
        List<Reservation> filteredList = new ArrayList<>();
        
        if (currentFilter.equals("all")) {
            filteredList.addAll(reservationList);
        } else {
            for (Reservation reservation : reservationList) {
                if (currentFilter.equals(reservation.getStatus())) {
                    filteredList.add(reservation);
                }
            }
        }
        
        // Update adapter
        adapter.updateReservations(filteredList);
        
        // Show/hide empty state
        if (filteredList.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onReservationClick(Reservation reservation) {
        // Show reservation details
        showReservationDetailsDialog(reservation);
    }

    @Override
    public void onStatusUpdateClick(Reservation reservation, String newStatus) {
        // Update reservation status
        progressBar.setVisibility(View.VISIBLE);
        
        reservationsRef.child(reservation.getReservationId())
                .child("status")
                .setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReservationManagementActivity.this, 
                            "Reservation status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReservationManagementActivity.this, 
                            "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showReservationDetailsDialog(Reservation reservation) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reservation_details, null);
        dialog.setContentView(dialogView);
        
        // Initialize dialog elements
        TextView idTextView = dialogView.findViewById(R.id.tv_reservation_id);
        TextView customerNameTextView = dialogView.findViewById(R.id.tv_customer_name);
        TextView customerEmailTextView = dialogView.findViewById(R.id.tv_customer_email);
        TextView dateTextView = dialogView.findViewById(R.id.tv_date);
        TextView timeTextView = dialogView.findViewById(R.id.tv_time);
        TextView guestsTextView = dialogView.findViewById(R.id.tv_guests);
        TextView statusTextView = dialogView.findViewById(R.id.tv_status);
        TextView notesTextView = dialogView.findViewById(R.id.tv_notes);
        Button confirmButton = dialogView.findViewById(R.id.btn_confirm);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        Button closeButton = dialogView.findViewById(R.id.btn_close);
        
        // Set dialog data
        idTextView.setText("Reservation #" + reservation.getReservationId());
        customerNameTextView.setText("Customer: " + reservation.getCustomerName());
        customerEmailTextView.setText("Email: " + reservation.getCustomerEmail());
        dateTextView.setText(dateFormat.format(reservation.getReservationDate()));
        timeTextView.setText("Time: " + reservation.getReservationTime());
        guestsTextView.setText("Guests: " + reservation.getNumberOfGuests());
        statusTextView.setText("Status: " + getReadableStatus(reservation.getStatus()));
        
        if (reservation.getSpecialNotes() != null && !reservation.getSpecialNotes().isEmpty()) {
            notesTextView.setText("Notes: " + reservation.getSpecialNotes());
            notesTextView.setVisibility(View.VISIBLE);
        } else {
            notesTextView.setVisibility(View.GONE);
        }
        
        // Show/hide action buttons based on current status
        if (reservation.isPending()) {
            confirmButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            
            confirmButton.setOnClickListener(v -> {
                onStatusUpdateClick(reservation, Reservation.STATUS_CONFIRMED);
                dialog.dismiss();
            });
            
            cancelButton.setOnClickListener(v -> {
                onStatusUpdateClick(reservation, Reservation.STATUS_CANCELLED);
                dialog.dismiss();
            });
        } else if (reservation.isConfirmed()) {
            confirmButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.VISIBLE);
            
            cancelButton.setOnClickListener(v -> {
                onStatusUpdateClick(reservation, Reservation.STATUS_CANCELLED);
                dialog.dismiss();
            });
        } else {
            confirmButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        }
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
