package com.example.demo.controller;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ReviewController(ReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    // Public: Get reviews for a property
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(ApiResponse.success(
                "Reviews fetched successfully",
                reviewService.getPropertyReviews(propertyId, pageable)
        ));
    }

    // Public: Get average rating
    @GetMapping("/property/{propertyId}/rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @PathVariable Long propertyId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Average rating fetched",
                reviewService.getAverageRating(propertyId)
        ));
    }

    // Authenticated: Add a review
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        ReviewResponse response = reviewService.addReview(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Review added successfully", response)
        );
    }

    // Authenticated: Delete own review
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        reviewService.deleteReview(id, userId);

        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}