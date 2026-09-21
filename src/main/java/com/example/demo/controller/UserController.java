package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.dto.request.ChangePasswordRequest;
import com.example.demo.dto.request.ForgotPasswordRequest;
import com.example.demo.dto.request.ResetPasswordRequest;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log =
        LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthService authService;

    // ✅ Manual constructor
    public UserController(UserService userService,AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // ─────────────────────────────────────────
    // GET CURRENT USER PROFILE
    // GET /api/users/me
    // ─────────────────────────────────────────
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Fetching profile for: {}", userDetails.getUsername());

        UserResponse user = userService
                .getUserByEmail(userDetails.getUsername());

        return ResponseEntity.ok(
            ApiResponse.success("User profile fetched", user));
    }

    // ─────────────────────────────────────────
    // UPDATE CURRENT USER PROFILE
    // PUT /api/users/me
    // ─────────────────────────────────────────
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Updating profile for: {}", userDetails.getUsername());

        UserResponse updated = userService.updateProfile(
                userDetails.getUsername(), request);

        return ResponseEntity.ok(
            ApiResponse.success("Profile updated successfully", updated));
    }

    // ─────────────────────────────────────────
    // UPLOAD PROFILE PICTURE
    // POST /api/users/me/profile-picture
    // ─────────────────────────────────────────
    @PostMapping("/me/profile-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading profile picture for: {}",
                userDetails.getUsername());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
            !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                        "Only image files are allowed (jpg, png, gif)"));
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                        "File size must be less than 5MB"));
        }

        UserResponse updated = userService.updateProfilePicture(
                userDetails.getUsername(), file);

        return ResponseEntity.ok(
            ApiResponse.success("Profile picture updated", updated));
    }

    // ─────────────────────────────────────────
    // CHANGE PASSWORD
    // PUT /api/users/me/change-password
    // ─────────────────────────────────────────
    @PutMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {

        log.info("Changing password for: {}", userDetails.getUsername());

        userService.changePassword(
                userDetails.getUsername(),
                currentPassword,
                newPassword);

        return ResponseEntity.ok(
            ApiResponse.success("Password changed successfully", null));
    }

    // ─────────────────────────────────────────
    // GET USER BY ID (Public)
    // GET /api/users/{id}
    // ─────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id) {

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(
            ApiResponse.success("User fetched successfully", user));
    }

    // ─────────────────────────────────────────
    // GET ALL USERS (Admin only)
    // GET /api/users
    // ─────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "")   String search) {

        log.info("Admin fetching all users - page: {}, size: {}", page, size);

        List<UserResponse> users = userService.getAllUsers(page, size, search);

        return ResponseEntity.ok(
            ApiResponse.success("Users fetched successfully", users));
    }

    // ─────────────────────────────────────────
    // BAN USER (Admin only)
    // PUT /api/users/{id}/ban
    // ─────────────────────────────────────────
    @PutMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @PathVariable Long id) {

        log.info("Admin banning user id: {}", id);
        userService.banUser(id);

        return ResponseEntity.ok(
            ApiResponse.success("User banned successfully", null));
    }

    // ─────────────────────────────────────────
    // UNBAN USER (Admin only)
    // PUT /api/users/{id}/unban
    // ─────────────────────────────────────────
    @PutMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unbanUser(
            @PathVariable Long id) {

        log.info("Admin unbanning user id: {}", id);
        userService.unbanUser(id);

        return ResponseEntity.ok(
            ApiResponse.success("User unbanned successfully", null));
    }

    // ─────────────────────────────────────────
    // DELETE USER (Admin only)
    // DELETE /api/users/{id}
    // ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id) {

        log.info("Admin deleting user id: {}", id);
        userService.deleteUser(id);

        return ResponseEntity.ok(
            ApiResponse.success("User deleted successfully", null));
    }

    // ─────────────────────────────────────────
    // PROMOTE USER TO SELLER (Admin only)
    // PUT /api/users/{id}/promote
    // ─────────────────────────────────────────
    @PutMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> promoteToSeller(
            @PathVariable Long id) {

        log.info("Admin promoting user id: {} to SELLER", id);
        userService.promoteToSeller(id);

        return ResponseEntity.ok(
            ApiResponse.success("User promoted to Seller", null));
    }
    
 // ─────────────────────────────────────────
 // FORGOT PASSWORD
 // POST /api/auth/forgot-password
 // ─────────────────────────────────────────
 @PostMapping("/forgot-password")
 public ResponseEntity<ApiResponse<Void>> forgotPassword(
         @Valid @RequestBody ForgotPasswordRequest request) {

     authService.sendPasswordResetEmail(request.getEmail());

     return ResponseEntity.ok(
         ApiResponse.success(
             "Password reset link sent to your email.", null));
 }

 // ─────────────────────────────────────────
 // RESET PASSWORD
 // POST /api/auth/reset-password
 // ─────────────────────────────────────────
 @PostMapping("/reset-password")
 public ResponseEntity<ApiResponse<Void>> resetPassword(
         @Valid @RequestBody ResetPasswordRequest request) {

     // Check passwords match
     if (!request.getNewPassword()
                 .equals(request.getConfirmPassword())) {
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(
                     "Passwords do not match"));
     }

     authService.resetPassword(
         request.getToken(),
         request.getNewPassword());

     return ResponseEntity.ok(
         ApiResponse.success("Password reset successful.", null));
 }

 // ─────────────────────────────────────────
 // CHANGE PASSWORD
 // PUT /api/auth/change-password
 // ─────────────────────────────────────────
 @PutMapping("/change-password")
 @PreAuthorize("isAuthenticated()")
 public ResponseEntity<ApiResponse<Void>> changePassword(
         @AuthenticationPrincipal UserDetails userDetails,
         @Valid @RequestBody ChangePasswordRequest request) {

     // Check passwords match
     if (!request.getNewPassword()
                 .equals(request.getConfirmPassword())) {
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(
                     "Passwords do not match"));
     }

     userService.changePassword(
         userDetails.getUsername(),
         request.getCurrentPassword(),
         request.getNewPassword());

     return ResponseEntity.ok(
         ApiResponse.success(
             "Password changed successfully.", null));
 }
}