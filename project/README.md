# Restaurant Management System

A comprehensive Android-based Restaurant Management System using Firebase for authentication and real-time data storage with role-based access.

## Features

### Multi-User Support
- **Customers**: Make reservations, browse menu, place orders, provide feedback
- **Waiters**: Manage orders, track table assignments
- **Chefs**: View and update order status, manage cooking queue
- **Managers**: Staff management, inventory control, menu management, view reports

### Technical Features
- Firebase Authentication for secure login
- Real-time database updates using Firebase Realtime Database
- Role-based interface and permissions
- Intuitive and modern Material Design UI

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Connect your own Firebase project:
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project with package name `com.spu.restaurantmanagementsystem`
   - Download the `google-services.json` file and replace the existing one in the app directory
   - Enable Authentication (Email/Password) and Realtime Database in the Firebase Console

4. Build and run the application

## Default Login Credentials

The system needs initial setup with default credentials:

- **Manager**: manager@restaurant.com / manager123
- **Chef**: chef@restaurant.com / chef123
- **Waiter**: waiter@restaurant.com / waiter123
- **Customer**: Register a new account through the app