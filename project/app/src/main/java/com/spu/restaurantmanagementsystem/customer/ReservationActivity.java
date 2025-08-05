package com.spu.restaurantmanagementsystem.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

public class ReservationActivity extends AppCompatActivity implements ReservationAdapter.OnReservationClickListener {

    private RecyclerView reservationsRecyclerView;
    private Button newReservationButton;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    
    private DatabaseReference reservationsRef;
    private String userId;
    private String userName;
    private String userEmail;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        
        // Set up toolbar
        getSupportActionBar().setTitle("My Reservations");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        reservationsRef = FirebaseUtil.getReservationsRef();
        
        // Get user info from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getString(Constants.PREF_USER_ID, null);
        userName = preferences.getString(Constants.PREF_USER_NAME, "Customer");
        userEmail = FirebaseUtil.getCurrentUser().getEmail();
        
        // Initialize UI elements
        reservationsRecyclerView = findViewById(R.id.rv_reservations);
        newReservationButton = findViewById(R.id.btn_new_reservation);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        
        // Set up RecyclerView
        reservationList = new ArrayList<>();
        adapter = new ReservationAdapter(this, reservationList, this, Constants.ROLE_CUSTOMER);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(adapter);
        
        // Set up click listener for new reservation button
        newReservationButton.setOnClickListener(v -> showNewReservationDialog());
        
        // Load user's reservations
        loadReservations();
    }

    private void loadReservations() {
        progressBar.setVisibility(View.VISIBLE);
        
        reservationsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reservationList.clear();
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Reservation reservation = snapshot.getValue(Reservation.class);
                            if (reservation != null) {
                                reservationList.add(reservation);
                            }
                        }
                        
                        // Update UI
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        
                        // Show empty state if no reservations
                        if (reservationList.isEmpty()) {
                            emptyStateTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyStateTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ReservationActivity.this, 
                                      "Error loading reservations: " + databaseError.getMessage(), 
                                      Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNewReservationDialog() {
        // Create bottom sheet dialog
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_reservation, null);
        dialog.setContentView(dialogView);
        
        // Initialize dialog elements
        TextView dateTextView = dialogView.findViewById(R.id.tv_selected_date);
        Button selectDateButton = dialogView.findViewById(R.id.btn_select_date);
        Spinner timeSpinner = dialogView.findViewById(R.id.spinner_time);
        Spinner guestsSpinner = dialogView.findViewById(R.id.spinner_guests);
        EditText notesEditText = dialogView.findViewById(R.id.et_special_notes);
        Button submitButton = dialogView.findViewById(R.id.btn_submit_reservation);
        
        // Set up date selection
        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
        selectDateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ReservationActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            
            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
        
        // Set up time spinner
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Constants.getTimeSlots());
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
        
        // Set up guests spinner
        List<String> guestOptions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            guestOptions.add(i + (i == 1 ? " person" : " people"));
        }
        ArrayAdapter<String> guestsAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, guestOptions);
        guestsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        guestsSpinner.setAdapter(guestsAdapter);
        
        // Set up submit button
        submitButton.setOnClickListener(v -> {
            // Validate date (ensure it's not in the past)
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            if (selectedDate.before(today)) {
                Toast.makeText(ReservationActivity.this, "Please select a future date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get form data
            String timeSlot = timeSpinner.getSelectedItem().toString();
            int numberOfGuests = guestsSpinner.getSelectedItemPosition() + 1;
            String specialNotes = notesEditText.getText().toString().trim();
            
            // Create new reservation
            createReservation(selectedDate.getTime(), timeSlot, numberOfGuests, specialNotes);
            
            // Close dialog
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void createReservation(Date reservationDate, String reservationTime, 
                                  int numberOfGuests, String specialNotes) {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        
        // Generate a new key for the reservation
        String reservationId = FirebaseUtil.generateKey(reservationsRef);
        
        // Create reservation object
        Reservation newReservation = new Reservation(
                reservationId,
                userId,
                reservationDate,
                reservationTime,
                numberOfGuests,
                Reservation.STATUS_PENDING,
                userName,
                userEmail,
                specialNotes
        );
        
        // Save to Firebase
        reservationsRef.child(reservationId).setValue(newReservation)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReservationActivity.this, 
                                  "Reservation submitted successfully", 
                                  Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReservationActivity.this, 
                                  "Failed to create reservation: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onReservationClick(Reservation reservation) {
        // Show reservation details dialog
        showReservationDetailsDialog(reservation);
    }

    @Override
    public void onStatusUpdateClick(Reservation reservation, String newStatus) {
        // Update reservation status (cancel reservation)
        progressBar.setVisibility(View.VISIBLE);
        
        reservationsRef.child(reservation.getReservationId())
                .child("status")
                .setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReservationActivity.this, 
                                  "Reservation cancelled", 
                                  Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReservationActivity.this, 
                                  "Failed to update: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showReservationDetailsDialog(Reservation reservation) {
        // Create dialog for reservation details
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reservation_details, null);
        dialog.setContentView(dialogView);
        
        // Initialize dialog elements
        TextView idTextView = dialogView.findViewById(R.id.tv_reservation_id);
        TextView dateTextView = dialogView.findViewById(R.id.tv_date);
        TextView timeTextView = dialogView.findViewById(R.id.tv_time);
        TextView guestsTextView = dialogView.findViewById(R.id.tv_guests);
        TextView statusTextView = dialogView.findViewById(R.id.tv_status);
        TextView notesTextView = dialogView.findViewById(R.id.tv_notes);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel_reservation);
        Button closeButton = dialogView.findViewById(R.id.btn_close);
        
        // Set dialog data
        idTextView.setText("Reservation #" + reservation.getReservationId());
        dateTextView.setText(dateFormat.format(reservation.getReservationDate()));
        timeTextView.setText(reservation.getReservationTime());
        guestsTextView.setText(reservation.getNumberOfGuests() + 
                             (reservation.getNumberOfGuests() == 1 ? " person" : " people"));
        statusTextView.setText(getReadableStatus(reservation.getStatus()));
        
        if (reservation.getSpecialNotes() != null && !reservation.getSpecialNotes().isEmpty()) {
            notesTextView.setText(reservation.getSpecialNotes());
        } else {
            notesTextView.setText("No special notes");
        }
        
        // Show cancel button only for pending or confirmed reservations
        if (reservation.isPending() || reservation.isConfirmed()) {
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(v -> {
                onStatusUpdateClick(reservation, Reservation.STATUS_CANCELLED);
                dialog.dismiss();
            });
        } else {
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
