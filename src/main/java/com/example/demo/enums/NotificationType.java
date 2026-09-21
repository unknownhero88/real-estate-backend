package com.example.demo.enums;

public enum NotificationType {
    INQUIRY_RECEIVED,     // Seller receives when buyer sends inquiry
    PROPERTY_APPROVED,    // Seller receives when property is approved
    PROPERTY_REJECTED,    // Seller receives when property is rejected
    INQUIRY_CLOSED,       // Buyer receives when seller closes inquiry
    WELCOME,              // New user welcome
    GENERAL               // General broadcast
}