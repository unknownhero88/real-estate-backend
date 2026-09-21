package com.example.demo.service.impl;

import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log =
        LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Manual constructor
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─────────────────────────────────────────
    // GET USER BY EMAIL
    // ─────────────────────────────────────────
    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found with email: " + email));
        return mapToResponse(user);
    }

    // ─────────────────────────────────────────
    // GET USER BY ID
    // ─────────────────────────────────────────
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found with id: " + id));
        return mapToResponse(user);
    }

    // ─────────────────────────────────────────
    // UPDATE PROFILE
    // ─────────────────────────────────────────
    @Override
    @Transactional
    public UserResponse updateProfile(String email,
                                      UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());

        if (request.getPhone() != null &&
            !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);
        log.info("Profile updated for: {}", email);
        return mapToResponse(user);
    }

    // ─────────────────────────────────────────
    // UPDATE PROFILE PICTURE
    // ─────────────────────────────────────────
    @Override
    @Transactional
    public UserResponse updateProfilePicture(String email,
                                             MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        try {
            // Save file to uploads folder
            String uploadDir  = "uploads/profiles/";
            String fileName   = UUID.randomUUID() + "_"
                                + file.getOriginalFilename();
            Path   uploadPath = Paths.get(uploadDir);

            // Create directory if not exists
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            // Update user profile picture path
            user.setProfilePicture(uploadDir + fileName);
            userRepository.save(user);

            log.info("Profile picture updated for: {}", email);
            return mapToResponse(user);

        } catch (IOException e) {
            log.error("Failed to upload profile picture: {}",
                    e.getMessage());
            throw new RuntimeException(
                "Failed to upload profile picture");
        }
    }

    // ─────────────────────────────────────────
    // CHANGE PASSWORD
    // ─────────────────────────────────────────
    @Override
    @Transactional
    public void changePassword(String email,
                               String currentPassword,
                               String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException(
                "Current password is incorrect");
        }

        // Validate new password is different
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                "New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for: {}", email);
    }

    // ─────────────────────────────────────────
    // GET ALL USERS (Admin)
    // ─────────────────────────────────────────
    @Override
    public List<UserResponse> getAllUsers(int page,
                                         int size,
                                         String search) {
        Pageable pageable = PageRequest.of(page, size);

        if (search != null && !search.isEmpty()) {
            return userRepository
                    .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search, search, pageable)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return userRepository.findAll(pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // BAN USER
    // ─────────────────────────────────────────
    @Override
    @Transactional
    public void banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot ban an admin user");
        }

        user.setIsBanned(true);
        userRepository.save(user);
        log.info("User banned: {}", user.getEmail());
    }

    // ─────────────────────────────────────────
    // UNBAN USER
    // ─────────────────────────────────────────
    @Override
    @Transactional
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found with id: " + id));

        user.setIsBanned(false);
        userRepository.save(user);
        log.info("User unbanned: {}", user.getEmail());
    }

    // ─────────────────────────────────────────
    // DELETE USER
    // ─────────────────────────────────────────
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(
                "Cannot delete an admin user");
        }

        userRepository.delete(user);
        log.info("User deleted: {}", user.getEmail());
    }

    // ─────────────────────────────────────────
    // PROMOTE TO SELLER
    // ─────────────────────────────────────────
    @Override
    @Transactional
    public void promoteToSeller(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found with id: " + id));

        user.setRole(Role.SELLER);
        userRepository.save(user);
        log.info("User promoted to SELLER: {}", user.getEmail());
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPER — Map Entity to Response
    // ─────────────────────────────────────────
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setProfilePicture(user.getProfilePicture());
        response.setRole(user.getRole().name());
        response.setIsVerified(user.getIsVerified());
        response.setIsActive(user.getIsActive());
        response.setIsBanned(user.getIsBanned());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}