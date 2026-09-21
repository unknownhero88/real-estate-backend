package com.example.demo.service;

import com.example.demo.dto.response.WishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistService {

    void addToWishlist(Long userId, Long propertyId);

    void removeFromWishlist(Long userId, Long propertyId);

    Page<WishlistResponse> getMyWishlist(Long userId, Pageable pageable);

    boolean isInWishlist(Long userId, Long propertyId);
}