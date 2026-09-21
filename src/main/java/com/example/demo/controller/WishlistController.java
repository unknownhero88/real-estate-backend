package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.WishlistResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.WishlistService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    public WishlistController(WishlistService wishlistService, UserRepository userRepository) {
        this.wishlistService = wishlistService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        wishlistService.addToWishlist(userId, propertyId);

        return ResponseEntity.ok(ApiResponse.success("Property added to wishlist", null));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        wishlistService.removeFromWishlist(userId, propertyId);

        return ResponseEntity.ok(ApiResponse.success("Property removed from wishlist", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WishlistResponse>>> getMyWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("savedAt").descending());

        return ResponseEntity.ok(ApiResponse.success(
                "Wishlist fetched successfully",
                wishlistService.getMyWishlist(userId, pageable)
        ));
    }

    @GetMapping("/check/{propertyId}")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlist(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        boolean isSaved = wishlistService.isInWishlist(userId, propertyId);

        return ResponseEntity.ok(ApiResponse.success("Checked wishlist status", isSaved));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}