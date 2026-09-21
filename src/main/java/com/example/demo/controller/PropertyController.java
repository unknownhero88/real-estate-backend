package com.example.demo.controller;

import com.example.demo.dto.request.PropertyRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PropertyResponse;
import com.example.demo.service.PropertyService;
import com.example.demo.repository.UserRepository;
import com.example.demo.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.service.PropertyPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserRepository userRepository;
    private final PropertyPdfService pdfService;

    public PropertyController(PropertyService propertyService, UserRepository userRepository,PropertyPdfService pdfService) {
        this.propertyService = propertyService;
        this.userRepository = userRepository;
        this.pdfService = pdfService;
    }

    // ── PUBLIC ENDPOINTS ─────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PropertyResponse> result = propertyService.getAllProperties(pageable);

        return ResponseEntity.ok(ApiResponse.success("Properties fetched successfully", result));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> searchProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String listingType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PropertyResponse> result = propertyService.searchProperties(
                city, type, listingType, minPrice, maxPrice, bedrooms, pageable);

        return ResponseEntity.ok(ApiResponse.success("Search results", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Property fetched", propertyService.getPropertyById(id)));
    }

    // ── SELLER ENDPOINTS ─────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getSellerId(userDetails);
        PropertyResponse response = propertyService.createProperty(request, sellerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getSellerId(userDetails);
        PropertyResponse response = propertyService.updateProperty(id, request, sellerId);

        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getSellerId(userDetails);
        propertyService.deleteProperty(id, sellerId);
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
    }

    @GetMapping("/my-listings")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> myListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getSellerId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(ApiResponse.success("My listings fetched",
                propertyService.getSellerProperties(sellerId, pageable)));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getSellerId(userDetails);
        List<String> urls = propertyService.uploadImages(id, files, sellerId);

        return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", urls));
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getSellerId(userDetails);
        propertyService.deleteImage(imageId, sellerId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }

    // ── HELPER METHOD ────────────────────────────────────────
    private Long getSellerId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    // Download Property as PDF (Public)
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPropertyPdf(@PathVariable Long id) {
        byte[] pdfBytes = pdfService.generatePropertyPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=property-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}