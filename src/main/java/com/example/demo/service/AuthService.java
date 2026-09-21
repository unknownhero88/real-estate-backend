package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthResponse;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void verifyEmail(String token);

    void sendPasswordResetEmail(String email);

    void resetPassword(String token, String newPassword);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}