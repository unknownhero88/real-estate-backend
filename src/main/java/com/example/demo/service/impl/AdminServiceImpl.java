package com.example.demo.service.impl;

import com.example.demo.dto.response.*;
import com.example.demo.entity.*;
import com.example.demo.enums.NotificationType;
import com.example.demo.enums.PropertyStatus;
import com.example.demo.enums.Role;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final InquiryRepository inquiryRepository;
    private final NotificationServiceImpl notificationService;

    public AdminServiceImpl(UserRepository userRepository,
                            PropertyRepository propertyRepository,
                            InquiryRepository inquiryRepository,
                            NotificationServiceImpl notificationService) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.inquiryRepository = inquiryRepository;
        this.notificationService = notificationService;
    }

    @Override
    public AdminStatsResponse getPlatformStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalBuyers(userRepository.countByRole(Role.BUYER))
                .totalSellers(userRepository.countByRole(Role.SELLER))
                .totalAdmins(userRepository.countByRole(Role.ADMIN))
                .totalProperties(propertyRepository.count())
                .approvedProperties(propertyRepository.countByIsApprovedTrue())
                .pendingProperties(propertyRepository.countByIsApprovedFalseAndIsActiveTrue())
                .totalInquiries(inquiryRepository.count())
                .bannedUsers(userRepository.countByIsBannedTrue())
                .build();
    }

    @Override
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        Page<User> users = (search != null && !search.isBlank())
                ? userRepository.searchUsers(search, pageable)
                : userRepository.findAll(pageable);

        return users.map(this::toUserResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return toUserResponse(findUser(id));
    }

    @Override
    public void banUser(Long id) {
        User user = findUser(id);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot ban an admin");
        }
        user.setIsBanned(true);
        userRepository.save(user);
        log.info("User banned by admin: {}", id);
    }

    @Override
    public void unbanUser(Long id) {
        User user = findUser(id);
        user.setIsBanned(false);
        userRepository.save(user);
        log.info("User unbanned by admin: {}", id);
    }

    @Override
    public void deleteUser(Long id) {
        User user = findUser(id);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot delete an admin");
        }
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User soft-deleted by admin: {}", id);
    }

    @Override
    public void promoteToSeller(Long id) {
        User user = findUser(id);
        user.setRole(Role.SELLER);
        userRepository.save(user);
        log.info("User {} promoted to SELLER", id);
    }

    @Override
    public void demoteToBuyer(Long id) {
        User user = findUser(id);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot demote an admin");
        }
        user.setRole(Role.BUYER);
        userRepository.save(user);
        log.info("User {} demoted to BUYER", id);
    }

    @Override
    public Page<PropertyResponse> getAllProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(this::toPropertyResponse);
    }

    @Override
    public Page<PropertyResponse> getPendingProperties(Pageable pageable) {
        return propertyRepository.findByIsApprovedFalseAndIsActiveTrue(pageable)
                .map(this::toPropertyResponse);
    }

    @Override
    public void approveProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (!propertyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Property not found");
        }
        propertyRepository.approveProperty(id);
        notificationService.createNotification(
                property.getSeller().getId(),
                "✅ Property Approved!",
                "Your property '" + property.getTitle() + "' is now live on the platform.",
                NotificationType.PROPERTY_APPROVED,
                property.getId()
        );
        log.info("Property approved: {}", id);
    }


    @Override
    public void rejectProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (property.getStatus() == PropertyStatus.REJECTED) {
            throw new IllegalStateException("Property is already rejected");
        }

        property.setStatus(PropertyStatus.REJECTED);
        propertyRepository.save(property);

        // ✅ Send Notification to Seller
        notificationService.createNotification(
                property.getSeller().getId(),
                "❌ Property Rejected",
                "Your property '" + property.getTitle() + "' was rejected. ",
                NotificationType.PROPERTY_REJECTED,
                property.getId()
        );

        log.info("Property {} rejected by admin.", id);
    }

    // ==================== PRIVATE HELPERS ====================

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toUserResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setFullName(u.getFullName());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setRole(u.getRole().name());
        r.setIsVerified(u.getIsVerified());
        r.setIsActive(u.getIsActive());
        r.setIsBanned(u.getIsBanned());
        r.setProfilePicture(u.getProfilePicture());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }

    private PropertyResponse toPropertyResponse(Property p) {
        return PropertyResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .price(p.getPrice())
                .type(p.getType() != null ? p.getType().name() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .listingType(p.getListingType() != null ? p.getListingType().name() : null)
                .city(p.getCity())
                .state(p.getState())
                .isApproved(p.getIsApproved())
                .viewCount(p.getViewCount())
                .sellerId(p.getSeller().getId())
                .sellerName(p.getSeller().getFullName())
                .sellerEmail(p.getSeller().getEmail())
                .createdAt(p.getCreatedAt())
                .build();
    }
}