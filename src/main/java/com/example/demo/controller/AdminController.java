package com.example.demo.controller;

import com.example.demo.dto.response.*;
import com.example.demo.service.AdminService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ── PLATFORM STATISTICS ─────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(
                ApiResponse.success("Platform stats fetched", adminService.getPlatformStats())
        );
    }

    // ── USER MANAGEMENT ─────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched", adminService.getAllUsers(search, pageable))
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User fetched", adminService.getUserById(id))
        );
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success("User banned successfully", null));
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unbanned successfully", null));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<ApiResponse<Void>> promoteToSeller(@PathVariable Long id) {
        adminService.promoteToSeller(id);
        return ResponseEntity.ok(ApiResponse.success("User promoted to Seller", null));
    }

    @PutMapping("/users/{id}/demote")
    public ResponseEntity<ApiResponse<Void>> demoteToBuyer(@PathVariable Long id) {
        adminService.demoteToBuyer(id);
        return ResponseEntity.ok(ApiResponse.success("User demoted to Buyer", null));
    }

    // ── PROPERTY MODERATION ─────────────────────────────────
    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                ApiResponse.success("Properties fetched", adminService.getAllProperties(pageable))
        );
    }

    @GetMapping("/properties/pending")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getPendingProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(
                ApiResponse.success("Pending properties fetched", adminService.getPendingProperties(pageable))
        );
    }

    @PutMapping("/properties/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveProperty(@PathVariable Long id) {
        adminService.approveProperty(id);
        return ResponseEntity.ok(ApiResponse.success("Property approved successfully", null));
    }

    @PutMapping("/properties/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectProperty(@PathVariable Long id) {
        adminService.rejectProperty(id);
        return ResponseEntity.ok(ApiResponse.success("Property rejected successfully", null));
    }
}