package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Newsletter;
import com.example.demo.repository.NewsletterRepository;
import com.example.demo.service.EmailService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterRepository newsletterRepository;
    private final EmailService emailService;

    public NewsletterController(NewsletterRepository newsletterRepository,
                                EmailService emailService) {
        this.newsletterRepository = newsletterRepository;
        this.emailService = emailService;
    }

    // Public: Subscribe to newsletter
    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @RequestParam @Email @NotBlank String email) {

        if (newsletterRepository.existsByEmail(email)) {
            return ResponseEntity.ok(ApiResponse.success("Already subscribed!", null));
        }

        newsletterRepository.save(new Newsletter(email));

        // Send welcome email
        emailService.sendRawEmail(
                email,
                "Welcome to RealEstate Newsletter!",
                "<h2>Thank you for subscribing!</h2><p>You will now receive the latest property updates and offers.</p>"
        );

        return ResponseEntity.ok(ApiResponse.success("Successfully subscribed to newsletter!", null));
    }

    // Public: Unsubscribe
    @DeleteMapping("/unsubscribe")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @RequestParam @Email String email) {

        newsletterRepository.findByEmail(email).ifPresent(n -> {
            n.setIsActive(false);
            newsletterRepository.save(n);
        });

        return ResponseEntity.ok(ApiResponse.success("Successfully unsubscribed", null));
    }

    // ADMIN: Broadcast newsletter
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> broadcast(
            @RequestParam String subject,
            @RequestParam String htmlContent) {

        List<Newsletter> activeSubscribers = newsletterRepository.findByIsActiveTrue();
        int sentCount = 0;

        for (Newsletter sub : activeSubscribers) {
            try {
                emailService.sendRawEmail(sub.getEmail(), subject, htmlContent);
                sentCount++;
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Newsletter broadcast sent to " + sentCount + " subscribers", null));
    }

    // ADMIN: Get subscriber count
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getCount() {
        return ResponseEntity.ok(ApiResponse.success(
                "Active subscribers",
                newsletterRepository.countByIsActiveTrue()));
    }
}