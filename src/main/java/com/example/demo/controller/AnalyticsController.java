package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.InquiryRepository;
import com.example.demo.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final ReviewRepository reviewRepository;

    public AnalyticsController(PropertyRepository propertyRepository,
                               UserRepository userRepository,
                               InquiryRepository inquiryRepository,
                               ReviewRepository reviewRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.inquiryRepository = inquiryRepository;
        this.reviewRepository = reviewRepository;
    }

    // ADMIN: Properties by Type
    @GetMapping("/admin/properties-by-type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> propertiesByType() {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("APARTMENT", propertyRepository.countByType(com.example.demo.enums.PropertyType.APARTMENT));
        data.put("HOUSE", propertyRepository.countByType(com.example.demo.enums.PropertyType.HOUSE));
        data.put("VILLA", propertyRepository.countByType(com.example.demo.enums.PropertyType.VILLA));
        data.put("PLOT", propertyRepository.countByType(com.example.demo.enums.PropertyType.PLOT));
        data.put("COMMERCIAL", propertyRepository.countByType(com.example.demo.enums.PropertyType.COMMERCIAL));

        return ResponseEntity.ok(ApiResponse.success("Properties by type", data));
    }

    // ADMIN: Top Cities
    @GetMapping("/admin/top-cities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> topCities() {
        List<Object[]> raw = propertyRepository.findTopCities();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("city", row[0]);
            m.put("count", row[1]);
            result.add(m);
        }
        return ResponseEntity.ok(ApiResponse.success("Top cities", result));
    }

    // SELLER: My Listing Stats
    @GetMapping("/seller/listing-stats")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> sellerListingStats(
            @AuthenticationPrincipal UserDetails ud) {

        // Implementation depends on your UserRepository method to get current user ID
        // (You can add this later)
        return ResponseEntity.ok(ApiResponse.success("Seller stats", new ArrayList<>()));
    }
}