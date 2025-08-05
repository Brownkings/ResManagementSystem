package com.spu.restaurantmanagementsystem.waiter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.models.Reservation;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TableAssignmentActivity extends AppCompatActivity {

    private RecyclerView tableAssignmentsRecyclerView;
    private TextView dateTextView, emptyStateTextView;
    private ProgressBar progressBar;
    
    private List<TableAssignment> tableAssignments;
    private TableAssignmentAdapter adapter;
    
    private DatabaseReference reservationsRef;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    private Date currentDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_assignment);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Table Assignments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        reservationsRef = FirebaseUtil.getReservationsRef();
        
        // Initialize UI elements
        tableAssignmentsRecyclerView = findViewById(R.id.rv_table_assignments);
        dateTextView = findViewById(R.id.tv_date);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set current date
        dateTextView.setText(dateFormat.format(currentDate));
        
        // Set up RecyclerView
        tableAssignments = new ArrayList<>();
        adapter = new TableAssignmentAdapter(this, tableAssignments);
        tableAssignmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableAssignmentsRecyclerView.setAdapter(adapter);
        
        // Load table assignments
        loadTableAssignments();
    }

    private void loadTableAssignments() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get confirmed reservations for today
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(currentDate);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(currentDate);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        
        reservationsRef.orderByChild("reservationDate")
                .startAt(startOfDay.getTimeInMillis())
                .endAt(endOfDay.getTimeInMillis())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Create a map of table assignments
                        Map<Integer, List<TableReservation>> tableMap = new HashMap<>();
                        for (int i = Constants.MIN_TABLE_NUMBER; i <= Constants.MAX_TABLE_NUMBER; i++) {
                            tableMap.put(i, new ArrayList<>());
                        }
                        
                        // Process reservations
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Reservation reservation = snapshot.getValue(Reservation.class);
                            if (reservation != null && reservation.isConfirmed()) {
                                // Assign to a table (simplified - in real app, would use proper table assignment logic)
                                int tableNumber = (Integer.parseInt(reservation.getReservationId()) % Constants.MAX_TABLE_NUMBER) + 1;
                                
                                TableReservation tableReservation = new TableReservation(
                                        reservation.getReservationId(),
                                        reservation.getCustomerName(),
                                        reservation.getReservationTime(),
                                        reservation.getNumberOfGuests()
                                );
                                
                                tableMap.get(tableNumber).add(tableReservation);
                            }
                        }
                        
                        // Convert map to list
                        tableAssignments.clear();
                        for (int tableNumber : tableMap.keySet()) {
                            tableAssignments.add(new TableAssignment(
                                    tableNumber,
                                    tableMap.get(tableNumber)
                            ));
                        }
                        
                        // Update UI
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        
                        // Show empty state if needed
                        boolean hasReservations = tableAssignments.stream()
                                .anyMatch(t -> !t.getReservations().isEmpty());
                        
                        if (!hasReservations) {
                            emptyStateTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyStateTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TableAssignmentActivity.this, 
                                "Error loading table assignments: " + databaseError.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    // Model classes
    public static class TableAssignment {
        private int tableNumber;
        private List<TableReservation> reservations;
        
        public TableAssignment(int tableNumber, List<TableReservation> reservations) {
            this.tableNumber = tableNumber;
            this.reservations = reservations;
        }
        
        public int getTableNumber() {
            return tableNumber;
        }
        
        public List<TableReservation> getReservations() {
            return reservations;
        }
    }
    
    public static class TableReservation {
        private String reservationId;
        private String customerName;
        private String time;
        private int numberOfGuests;
        
        public TableReservation(String reservationId, String customerName, String time, int numberOfGuests) {
            this.reservationId = reservationId;
            this.customerName = customerName;
            this.time = time;
            this.numberOfGuests = numberOfGuests;
        }
        
        public String getReservationId() {
            return reservationId;
        }
        
        public String getCustomerName() {
            return customerName;
        }
        
        public String getTime() {
            return time;
        }
        
        public int getNumberOfGuests() {
            return numberOfGuests;
        }
    }
    
    // Adapter
    public static class TableAssignmentAdapter extends RecyclerView.Adapter<TableAssignmentAdapter.TableAssignmentViewHolder> {
        
        private List<TableAssignment> tableAssignments;
        private android.content.Context context;
        
        public TableAssignmentAdapter(android.content.Context context, List<TableAssignment> tableAssignments) {
            this.context = context;
            this.tableAssignments = tableAssignments;
        }
        
        @NonNull
        @Override
        public TableAssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(context).inflate(R.layout.item_table_assignment, parent, false);
            return new TableAssignmentViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull TableAssignmentViewHolder holder, int position) {
            TableAssignment assignment = tableAssignments.get(position);
            
            holder.tableNumberTextView.setText("Table " + assignment.getTableNumber());
            
            if (assignment.getReservations().isEmpty()) {
                holder.statusTextView.setText("No reservations");
                holder.statusTextView.setTextColor(context.getResources().getColor(R.color.colorAvailable));
                holder.reservationsTextView.setVisibility(View.GONE);
            } else {
                holder.statusTextView.setText("Reserved");
                holder.statusTextView.setTextColor(context.getResources().getColor(R.color.colorUnavailable));
                holder.reservationsTextView.setVisibility(View.VISIBLE);
                
                StringBuilder reservationsText = new StringBuilder();
                for (TableReservation reservation : assignment.getReservations()) {
                    reservationsText.append(reservation.getTime())
                            .append(" - ")
                            .append(reservation.getCustomerName())
                            .append(" (")
                            .append(reservation.getNumberOfGuests())
                            .append(" guests)\n");
                }
                
                holder.reservationsTextView.setText(reservationsText.toString().trim());
            }
        }
        
        @Override
        public int getItemCount() {
            return tableAssignments.size();
        }
        
        public static class TableAssignmentViewHolder extends RecyclerView.ViewHolder {
            TextView tableNumberTextView;
            TextView statusTextView;
            TextView reservationsTextView;
            
            public TableAssignmentViewHolder(@NonNull View itemView) {
                super(itemView);
                tableNumberTextView = itemView.findViewById(R.id.tv_table_number);
                statusTextView = itemView.findViewById(R.id.tv_status);
                reservationsTextView = itemView.findViewById(R.id.tv_reservations);
            }
        }
    }
}
