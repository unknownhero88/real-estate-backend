package com.example.demo.controller;

import com.example.demo.dto.request.InquiryRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.InquiryResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;
    private final UserRepository userRepository;

    public InquiryController(InquiryService inquiryService, UserRepository userRepository) {
        this.inquiryService = inquiryService;
        this.userRepository = userRepository;
    }

    // ── BUYER ENDPOINTS ──────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('BUYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryResponse>> sendInquiry(
            @Valid @RequestBody InquiryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long buyerId = getUserId(userDetails);
        InquiryResponse response = inquiryService.sendInquiry(request, buyerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inquiry sent successfully", response));
    }

    @GetMapping("/my-inquiries")
    @PreAuthorize("hasRole('BUYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> myInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long buyerId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(ApiResponse.success("My inquiries fetched",
                inquiryService.getBuyerInquiries(buyerId, pageable)));
    }

    // ── SELLER ENDPOINTS ─────────────────────────────────────

    @GetMapping("/incoming")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> incomingInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(ApiResponse.success("Incoming inquiries fetched",
                inquiryService.getSellerInquiries(sellerId, pageable)));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> closeInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = getUserId(userDetails);
        inquiryService.closeInquiry(id, sellerId);
        return ResponseEntity.ok(ApiResponse.success("Inquiry closed successfully", null));
    }

    // ── HELPER METHOD ────────────────────────────────────────
    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}