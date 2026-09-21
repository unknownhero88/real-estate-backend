package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PropertyResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommendationController(
            RecommendationService recommendationService,
            UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    // Personalized recommendations (for logged-in user)
    @GetMapping("/for-me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> forMe(
            @RequestParam(defaultValue = "6") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();

        List<PropertyResponse> recommendations = recommendationService.getRecommendations(userId, limit);

        return ResponseEntity.ok(ApiResponse.success("Personalized recommendations", recommendations));
    }

    // Similar properties (public)
    @GetMapping("/similar/{propertyId}")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> similar(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "4") int limit) {

        List<PropertyResponse> similar = recommendationService.getSimilarProperties(propertyId, limit);

        return ResponseEntity.ok(ApiResponse.success("Similar properties", similar));
    }
}