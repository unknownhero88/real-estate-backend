package com.example.demo.service;

public interface EmailService {

    void sendVerificationEmail(String to, String name, String verifyLink);

    void sendPasswordResetEmail(String to, String name, String resetLink);

    void sendRawEmail(String to, String subject, String htmlContent);


}