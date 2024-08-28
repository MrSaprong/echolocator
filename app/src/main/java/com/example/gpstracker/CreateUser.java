package com.example.gpstracker;

public class CreateUser {
    public String name;
    public String email;
    public String password;
    public String code;
    public String issharing; // Assuming this stores "true" or "false"
    public String lat;
    public String lng;
    public String imageUrl;
    public String userId;

    // Constructor
    public CreateUser(String name, String email, String password, String code, String issharing, String lat, String lng, String imageUrl, String userId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.code = code;
        this.issharing = issharing;
        this.lat = lat;
        this.lng = lng;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    // Default constructor required for calls to DataSnapshot.getValue(CreateUser.class)
    public CreateUser() {
    }

    // Method to check if the user is sharing their location
    public boolean isSharing() {
        return "true".equalsIgnoreCase(this.issharing); // Compare ignoring case sensitivity
    }
}
