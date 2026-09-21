package com.example.demo.service.impl;

import com.example.demo.dto.response.WishlistResponse;
import com.example.demo.entity.Property;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               UserRepository userRepository,
                               PropertyRepository propertyRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    public void addToWishlist(Long userId, Long propertyId) {
        if (wishlistRepository.existsByUserIdAndPropertyId(userId, propertyId)) {
            return; // already saved
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        wishlistRepository.save(new Wishlist(user, property));
        log.info("User {} added property {} to wishlist", userId, propertyId);
    }

    @Override
    public void removeFromWishlist(Long userId, Long propertyId) {
        wishlistRepository.deleteByUserIdAndPropertyId(userId, propertyId);
        log.info("User {} removed property {} from wishlist", userId, propertyId);
    }

    @Override
    public Page<WishlistResponse> getMyWishlist(Long userId, Pageable pageable) {
        return wishlistRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public boolean isInWishlist(Long userId, Long propertyId) {
        return wishlistRepository.existsByUserIdAndPropertyId(userId, propertyId);
    }

    private WishlistResponse toResponse(Wishlist w) {
        Property p = w.getProperty();
        String primaryImg = p.getImages().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .map(i -> i.getImageUrl())
                .findFirst().orElse(null);

        return WishlistResponse.builder()
                .id(w.getId())
                .propertyId(p.getId())
                .propertyTitle(p.getTitle())
                .propertyCity(p.getCity())
                .propertyState(p.getState())
                .primaryImageUrl(primaryImg)
                .price(p.getPrice())
                .listingType(p.getListingType() != null ? p.getListingType().name() : null)
                .type(p.getType() != null ? p.getType().name() : null)
                .savedAt(w.getSavedAt())
                .build();
    }
}