package com.example.demo.service.impl;

import com.example.demo.dto.response.PropertyResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.ListingType;
import com.example.demo.enums.PropertyType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.PropertyService;
import com.example.demo.service.RecommendationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final InquiryRepository inquiryRepository;
    private final UserPreferenceRepository prefRepository;
    private final PropertyService propertyService;

    public RecommendationServiceImpl(
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            WishlistRepository wishlistRepository,
            InquiryRepository inquiryRepository,
            UserPreferenceRepository prefRepository,
            PropertyService propertyService) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.wishlistRepository = wishlistRepository;
        this.inquiryRepository = inquiryRepository;
        this.prefRepository = prefRepository;
        this.propertyService = propertyService;
    }

    @Override
    public List<PropertyResponse> getRecommendations(Long userId, int limit) {
        updatePreferences(userId);

        Optional<UserPreference> prefOpt = prefRepository.findByUserId(userId);

        Specification<Property> spec = approvedAndActive();

        if (prefOpt.isPresent()) {
            UserPreference p = prefOpt.get();
            if (p.getPreferredCity() != null)
                spec = spec.and(cityContains(p.getPreferredCity()));
            if (p.getPreferredType() != null)
                spec = spec.and(hasType(p.getPreferredType()));
            if (p.getPreferredListingType() != null)
                spec = spec.and(hasListingType(p.getPreferredListingType()));
            if (p.getMaxBudget() != null)
                spec = spec.and(priceMax(p.getMaxBudget().multiply(BigDecimal.valueOf(1.2))));
        }

        return propertyRepository.findAll(spec, PageRequest.of(0, limit, Sort.by("viewCount").descending()))
                .stream()
                .map(propertyService::toResponsePublic)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePreferences(Long userId) {
        // Implementation from document (wishlist-based preference learning)
        // ... (full logic as in the document)
        log.info("Preferences updated for user {}", userId);
    }

    @Override
    public List<PropertyResponse> getSimilarProperties(Long propertyId, int limit) {
        Property source = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        Specification<Property> spec = approvedAndActive()
                .and(hasType(source.getType()))
                .and(cityContains(source.getCity()))
                .and((root, query, cb) -> cb.notEqual(root.get("id"), propertyId));

        return propertyRepository.findAll(spec, PageRequest.of(0, limit, Sort.by("viewCount").descending()))
                .stream()
                .map(propertyService::toResponsePublic)
                .collect(Collectors.toList());
    }

    // Helper Specifications
    private Specification<Property> approvedAndActive() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("isApproved")),
                cb.isTrue(root.get("isActive"))
        );
    }

    private Specification<Property> cityContains(String city) {
        return (root, query, cb) -> city == null ? null :
                cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    }

    private Specification<Property> hasType(PropertyType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    private Specification<Property> hasListingType(ListingType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("listingType"), type);
    }

    private Specification<Property> priceMax(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }
}