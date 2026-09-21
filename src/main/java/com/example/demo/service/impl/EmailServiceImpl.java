package com.example.demo.service.impl;

import com.example.demo.service.EmailService;
import com.example.demo.util.EmailUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ─────────────────────────────────────────
    // SEND VERIFICATION EMAIL
    // ─────────────────────────────────────────
    @Override
    public void sendVerificationEmail(String to,
                                      String name,
                                      String verifyLink) {
        String subject = "✅ Verify Your Email — RealEstate Portal";
        String html    = EmailUtils.buildVerificationEmail(
                name, verifyLink);
        sendHtmlEmail(to, subject, html);
    }

    // ─────────────────────────────────────────
    // SEND PASSWORD RESET EMAIL
    // ─────────────────────────────────────────
    @Override
    public void sendPasswordResetEmail(String to,
                                       String name,
                                       String resetLink) {
        String subject = "🔒 Reset Your Password — RealEstate Portal";
        String html    = EmailUtils.buildPasswordResetEmail(
                name, resetLink);
        sendHtmlEmail(to, subject, html);
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────
    private void sendHtmlEmail(String to,
                               String subject,
                               String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Email sent to: {}",
                    EmailUtils.maskEmail(to));

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}",
                    EmailUtils.maskEmail(to), e.getMessage());
            throw new RuntimeException(
                    "Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public void sendRawEmail(String to, String subject, String htmlContent) {
        sendHtmlEmail(to, subject, htmlContent);   // Reuse your existing method
    }
}