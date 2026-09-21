package com.example.demo.service;

import com.example.demo.dto.response.AdminStatsResponse;
import com.example.demo.dto.response.PropertyResponse;
import com.example.demo.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface AdminService {

    // Platform Statistics
    AdminStatsResponse getPlatformStats();

    // User Management
    Page<UserResponse> getAllUsers(String search, Pageable pageable);
    UserResponse getUserById(Long id);
    void banUser(Long id);
    void unbanUser(Long id);
    void deleteUser(Long id);
    void promoteToSeller(Long id);
    void demoteToBuyer(Long id);

    // Property Moderation
    Page<PropertyResponse> getAllProperties(Pageable pageable);
    Page<PropertyResponse> getPendingProperties(Pageable pageable);
    void approveProperty(Long id);
    void rejectProperty(Long id);


}