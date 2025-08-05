package com.spu.restaurantmanagementsystem.manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.models.MenuItem;
import com.spu.restaurantmanagementsystem.models.Order;
import com.spu.restaurantmanagementsystem.models.OrderItem;
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

public class ReportsActivity extends AppCompatActivity {

    private TextView startDateTextView, endDateTextView;
    private Button startDateButton, endDateButton, generateReportButton;
    private ProgressBar progressBar;
    private BarChart salesBarChart;
    private PieChart categoryPieChart;
    private TextView totalSalesTextView, totalOrdersTextView, avgOrderValueTextView;
    
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    
    private DatabaseReference ordersRef;
    private DatabaseReference orderItemsRef;
    private DatabaseReference menuItemsRef;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        ordersRef = FirebaseUtil.getOrdersRef();
        orderItemsRef = FirebaseUtil.getOrderItemsRef();
        menuItemsRef = FirebaseUtil.getMenuItemsRef();
        
        // Initialize UI elements
        startDateTextView = findViewById(R.id.tv_start_date);
        endDateTextView = findViewById(R.id.tv_end_date);
        startDateButton = findViewById(R.id.btn_select_start_date);
        endDateButton = findViewById(R.id.btn_select_end_date);
        generateReportButton = findViewById(R.id.btn_generate_report);
        progressBar = findViewById(R.id.progress_bar);
        salesBarChart = findViewById(R.id.chart_sales);
        categoryPieChart = findViewById(R.id.chart_categories);
        totalSalesTextView = findViewById(R.id.tv_total_sales);
        totalOrdersTextView = findViewById(R.id.tv_total_orders);
        avgOrderValueTextView = findViewById(R.id.tv_avg_order_value);
        
        // Set default date range (last 7 days)
        startDateCalendar.add(Calendar.DAY_OF_MONTH, -7);
        updateDateDisplay();
        
        // Set up button click listeners
        setupClickListeners();
        
        // Initialize charts
        setupCharts();
    }

    private void setupClickListeners() {
        startDateButton.setOnClickListener(v -> {
            // Show date picker for start date
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ReportsActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        startDateCalendar.set(year, month, dayOfMonth);
                        updateDateDisplay();
                    },
                    startDateCalendar.get(Calendar.YEAR),
                    startDateCalendar.get(Calendar.MONTH),
                    startDateCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMaxDate(endDateCalendar.getTimeInMillis());
            datePickerDialog.show();
        });
        
        endDateButton.setOnClickListener(v -> {
            // Show date picker for end date
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ReportsActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        endDateCalendar.set(year, month, dayOfMonth);
                        updateDateDisplay();
                    },
                    endDateCalendar.get(Calendar.YEAR),
                    endDateCalendar.get(Calendar.MONTH),
                    endDateCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            datePickerDialog.show();
        });
        
        generateReportButton.setOnClickListener(v -> {
            // Generate reports for the selected date range
            generateReports();
        });
    }
    
    private void updateDateDisplay() {
        startDateTextView.setText(dateFormat.format(startDateCalendar.getTime()));
        endDateTextView.setText(dateFormat.format(endDateCalendar.getTime()));
    }
    
    private void setupCharts() {
        // Set up bar chart
        salesBarChart.getDescription().setEnabled(false);
        salesBarChart.setDrawGridBackground(false);
        salesBarChart.setDrawBarShadow(false);
        salesBarChart.setDrawValueAboveBar(true);
        salesBarChart.setPinchZoom(false);
        salesBarChart.setDrawGridBackground(false);
        
        XAxis xAxis = salesBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        
        salesBarChart.getAxisLeft().setDrawGridLines(false);
        salesBarChart.getAxisRight().setEnabled(false);
        salesBarChart.getLegend().setEnabled(false);
        
        // Set up pie chart
        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setUsePercentValues(true);
        categoryPieChart.setDrawHoleEnabled(true);
        categoryPieChart.setHoleColor(Color.WHITE);
        categoryPieChart.setHoleRadius(35f);
        categoryPieChart.setTransparentCircleRadius(40f);
        categoryPieChart.setDrawCenterText(true);
        categoryPieChart.setCenterText("Categories");
        categoryPieChart.setRotationAngle(0);
        categoryPieChart.setRotationEnabled(true);
        categoryPieChart.setHighlightPerTapEnabled(true);
        categoryPieChart.getLegend().setEnabled(false);
    }
    
    private void generateReports() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Adjust start and end date for query
        Calendar startOfDay = (Calendar) startDateCalendar.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        
        Calendar endOfDay = (Calendar) endDateCalendar.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        
        // Query orders between dates
        Query dateQuery = ordersRef.orderByChild("orderTime")
                .startAt(startOfDay.getTimeInMillis())
                .endAt(endOfDay.getTimeInMillis());
        
        dateQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Order> orders = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null && order.isServed()) {
                        orders.add(order);
                    }
                }
                
                if (orders.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReportsActivity.this, 
                            "No completed orders found in the selected date range", 
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Process order data
                processOrderData(orders);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReportsActivity.this, 
                        "Error generating reports: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void processOrderData(List<Order> orders) {
        // Calculate total sales and average order value
        double totalSales = 0;
        for (Order order : orders) {
            totalSales += order.getTotalAmount();
        }
        
        double avgOrderValue = orders.isEmpty() ? 0 : totalSales / orders.size();
        
        // Update summary text views
        totalSalesTextView.setText(String.format("Total Sales: R%.2f", totalSales));
        totalOrdersTextView.setText("Total Orders: " + orders.size());
        avgOrderValueTextView.setText(String.format("Avg. Order Value: R%.2f", avgOrderValue));
        
        // Prepare data for sales by day chart
        prepareSalesChart(orders);
        
        // Fetch order items for category breakdown
        fetchOrderItemsForOrders(orders);
    }
    
    private void prepareSalesChart(List<Order> orders) {
        // Create map to store sales by date
        Map<String, Double> salesByDay = new HashMap<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        
        // Calculate total days in range
        long startTime = startDateCalendar.getTimeInMillis();
        long endTime = endDateCalendar.getTimeInMillis();
        int totalDays = (int) ((endTime - startTime) / (1000 * 60 * 60 * 24)) + 1;
        
        // Initialize all days with zero sales
        Calendar tempCal = (Calendar) startDateCalendar.clone();
        for (int i = 0; i < totalDays; i++) {
            salesByDay.put(dayFormat.format(tempCal.getTime()), 0.0);
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Sum up sales by day
        for (Order order : orders) {
            String day = dayFormat.format(order.getOrderTime());
            Double currentSales = salesByDay.get(day);
            if (currentSales != null) {
                salesByDay.put(day, currentSales + order.getTotalAmount());
            }
        }
        
        // Convert to chart entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        
        tempCal = (Calendar) startDateCalendar.clone();
        for (int i = 0; i < totalDays; i++) {
            String day = dayFormat.format(tempCal.getTime());
            entries.add(new BarEntry(index, salesByDay.get(day).floatValue()));
            labels.add(day);
            index++;
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Create and style dataset
        BarDataSet dataSet = new BarDataSet(entries, "Sales by Day");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(true);
        
        // Create bar data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        // Set data to chart
        salesBarChart.setData(barData);
        salesBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        salesBarChart.invalidate(); // refresh
    }
    
    private void fetchOrderItemsForOrders(List<Order> orders) {
        // Create list of order IDs
        List<String> orderIds = new ArrayList<>();
        for (Order order : orders) {
            orderIds.add(order.getOrderId());
        }
        
        // Fetch menu items first to get category info
        menuItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, MenuItem> menuItemMap = new HashMap<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MenuItem menuItem = snapshot.getValue(MenuItem.class);
                    if (menuItem != null) {
                        menuItemMap.put(menuItem.getItemId(), menuItem);
                    }
                }
                
                // Now fetch order items
                if (!orderIds.isEmpty()) {
                    fetchAndProcessOrderItems(orderIds, menuItemMap);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReportsActivity.this, 
                        "Error loading menu items: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void fetchAndProcessOrderItems(List<String> orderIds, Map<String, MenuItem> menuItemMap) {
        // We'll need to do multiple queries as Firebase doesn't support OR conditions
        // For simplicity, we'll just do one query at a time
        
        if (orderIds.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        Map<String, Double> salesByCategory = new HashMap<>();
        
        // Function to process next order
        final int[] processedOrders = {0};
        
        for (String orderId : orderIds) {
            orderItemsRef.orderByChild("orderId").equalTo(orderId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                OrderItem orderItem = snapshot.getValue(OrderItem.class);
                                if (orderItem != null) {
                                    // Get menu item to determine category
                                    MenuItem menuItem = menuItemMap.get(orderItem.getItemId());
                                    if (menuItem != null) {
                                        String category = menuItem.getCategory();
                                        double subtotal = orderItem.getSubtotal();
                                        
                                        // Add to category totals
                                        Double currentTotal = salesByCategory.get(category);
                                        if (currentTotal == null) {
                                            salesByCategory.put(category, subtotal);
                                        } else {
                                            salesByCategory.put(category, currentTotal + subtotal);
                                        }
                                    }
                                }
                            }
                            
                            processedOrders[0]++;
                            
                            // If all orders processed, update chart
                            if (processedOrders[0] >= orderIds.size()) {
                                updateCategoryChart(salesByCategory);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            processedOrders[0]++;
                            
                            // If all orders processed, update chart
                            if (processedOrders[0] >= orderIds.size()) {
                                updateCategoryChart(salesByCategory);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }
    
    private void updateCategoryChart(Map<String, Double> salesByCategory) {
        if (salesByCategory.isEmpty()) {
            categoryPieChart.setVisibility(View.GONE);
            return;
        }
        
        categoryPieChart.setVisibility(View.VISIBLE);
        
        // Convert to chart entries
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : salesByCategory.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }
        
        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        
        // Create pie data
        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.WHITE);
        pieData.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(categoryPieChart));
        
        // Set data to chart
        categoryPieChart.setData(pieData);
        categoryPieChart.invalidate(); // refresh
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
