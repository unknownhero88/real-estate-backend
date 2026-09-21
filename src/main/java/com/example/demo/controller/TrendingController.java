package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PropertyResponse;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.service.PropertyService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trending")
public class TrendingController {

    private final PropertyRepository propertyRepository;
    private final PropertyService propertyService;

    public TrendingController(
            PropertyRepository propertyRepository,
            PropertyService propertyService) {
        this.propertyRepository = propertyRepository;
        this.propertyService = propertyService;
    }

    // Most Viewed Properties
    @GetMapping("/most-viewed")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> mostViewed(
            @RequestParam(defaultValue = "8") int limit) {

        List<PropertyResponse> properties = propertyRepository
                .findByIsApprovedTrueAndIsActiveTrue(
                        PageRequest.of(0, limit, Sort.by("viewCount").descending()))
                .stream()
                .map(propertyService::toResponsePublic)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Most Viewed Properties", properties));
    }

    // Newest Listings
    @GetMapping("/newest")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> newest(
            @RequestParam(defaultValue = "8") int limit) {

        List<PropertyResponse> properties = propertyRepository
                .findByIsApprovedTrueAndIsActiveTrue(
                        PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(propertyService::toResponsePublic)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Newest Properties", properties));
    }
}