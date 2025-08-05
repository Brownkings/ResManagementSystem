package com.spu.restaurantmanagementsystem.models;

import java.util.Date;

public class Reservation {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_CANCELLED = "cancelled";

    private String reservationId;
    private String userId;
    private Date reservationDate;
    private String reservationTime;
    private int numberOfGuests;
    private String status;
    private String customerName;
    private String customerEmail;
    private String specialNotes;

    // Empty constructor needed for Firebase
    public Reservation() {
    }

    public Reservation(String reservationId, String userId, Date reservationDate, 
                      String reservationTime, int numberOfGuests, String status,
                      String customerName, String customerEmail, String specialNotes) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.numberOfGuests = numberOfGuests;
        this.status = status;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.specialNotes = specialNotes;
    }

    // Getters and setters
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }
}
