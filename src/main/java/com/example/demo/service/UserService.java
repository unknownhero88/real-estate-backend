package com.example.demo.service;

import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse getUserByEmail(String email);

    UserResponse getUserById(Long id);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    UserResponse updateProfilePicture(String email, MultipartFile file);

    void changePassword(String email,
                        String currentPassword,
                        String newPassword);

    List<UserResponse> getAllUsers(int page, int size, String search);

    void banUser(Long id);

    void unbanUser(Long id);

    void deleteUser(Long id);

    void promoteToSeller(Long id);
}