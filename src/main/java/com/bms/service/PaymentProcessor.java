package com.bms.service;

public class PaymentProcessor {
    public boolean processPayment(String userId, double amount) {
        // Simulating a third-party payment gateway delay
        try {
            Thread.sleep(500); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true; // success assumed -> can be later extended
    }
}
