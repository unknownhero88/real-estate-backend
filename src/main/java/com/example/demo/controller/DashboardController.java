package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    // ─── BUYER DASHBOARD ─────────────────────
    @GetMapping("/buyer/dashboard")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<String>> buyerDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success("Welcome to Buyer Dashboard", null));
    }

    // ─── SELLER DASHBOARD ────────────────────
//    @GetMapping("/seller/dashboard")
//    @PreAuthorize("hasRole('SELLER')")
//    public ResponseEntity<ApiResponse<String>> sellerDashboard() {
//        return ResponseEntity.ok(
//                ApiResponse.success("Welcome to Seller Dashboard", null));
//    }

    // ─── ADMIN DASHBOARD ─────────────────────
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success("Welcome to Admin Dashboard", null));
    }
}