package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.SellerStatsResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SellerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
public class SellerController {

    private final SellerService sellerService;
    private final UserRepository userRepository;

    public SellerController(SellerService sellerService, UserRepository userRepository) {
        this.sellerService = sellerService;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SellerStatsResponse>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();

        return ResponseEntity.ok(
                ApiResponse.success("Seller stats fetched successfully",
                        sellerService.getSellerStats(sellerId))
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> dashboard() {
        return ResponseEntity.ok(
                ApiResponse.success("Welcome to Seller Dashboard", null)
        );
    }
}