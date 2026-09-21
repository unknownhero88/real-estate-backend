package com.example.demo.service.impl;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.NotificationType;
import com.example.demo.enums.Role;
import com.example.demo.enums.TokenType;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.AuthService;
import com.example.demo.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

	// ✅ Manual logger — NOT @Slf4j
	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

	// ✅ All final fields
	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final EmailService emailService;
	private final NotificationServiceImpl notificationService;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	// ✅ Manual constructor — assigns ALL final fields
	public AuthServiceImpl(UserRepository userRepository, TokenRepository tokenRepository,
			RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider, EmailService emailService, NotificationServiceImpl notificationService) {

		this.userRepository = userRepository;
		this.tokenRepository = tokenRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.emailService = emailService;
		this.notificationService = notificationService;
	}

	// ─────────────────────────────────────────
	// REGISTER
	// ─────────────────────────────────────────
	@Override
	@Transactional
	public void register(RegisterRequest request) {

		// 1. Check if email already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new UserAlreadyExistsException("An account with this email already exists");
		}

		// 2. Build and save user
		User user = new User();
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setPhone(request.getPhone());
		user.setRole(request.getRole() != null ? request.getRole() : Role.BUYER);
		user.setIsVerified(false);
		user.setIsActive(true);
		user.setIsBanned(false);

		userRepository.save(user);

		// 3. Create email verification token
		String token = UUID.randomUUID().toString();

		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setTokenType(TokenType.EMAIL_VERIFY);
		verificationToken.setUser(user);
		verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
		verificationToken.setIsUsed(false);

		tokenRepository.save(verificationToken);

		// 4. Send verification email
		String verifyLink = frontendUrl + "/verify-email?token=" + token;

		emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verifyLink);

		notificationService.createNotification(user.getId(), "Welcome to RealEstate! 🎉",
				"Hello " + user.getFullName() + ", thank you for joining us.", NotificationType.WELCOME, null);
		log.info("User registered: {}", user.getEmail());
	}

	// ─────────────────────────────────────────
	// LOGIN
	// ─────────────────────────────────────────
	@Override
	public AuthResponse login(LoginRequest request) {

		// 1. Find user
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

		// 2. Check password
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new ResourceNotFoundException("Invalid email or password");
		}

		// 3. Check verified
		if (!user.getIsVerified()) {
			throw new IllegalStateException("Please verify your email before logging in");
		}

		// 4. Check banned/active
		if (user.getIsBanned()) {
			throw new IllegalStateException("Your account has been banned");
		}
		if (!user.getIsActive()) {
			throw new IllegalStateException("Your account is deactivated");
		}

		// 5. Generate tokens
		String accessToken = jwtTokenProvider.generateAccessToken(user);
		String refreshToken = jwtTokenProvider.generateRefreshToken(user);

		// 6. Save refresh token to DB
		RefreshToken refreshTokenEntity = new RefreshToken();
		refreshTokenEntity.setToken(refreshToken);
		refreshTokenEntity.setUser(user);
		refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
		refreshTokenEntity.setIsRevoked(false);

		refreshTokenRepository.save(refreshTokenEntity);

		log.info("User logged in: {}", user.getEmail());

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer")
				.userId(user.getId()).email(user.getEmail()).fullName(user.getFullName()).role(user.getRole().name())
				.isVerified(user.getIsVerified()).build();
	}

	// ─────────────────────────────────────────
	// VERIFY EMAIL
	// ─────────────────────────────────────────
	@Override
	@Transactional
	public void verifyEmail(String token) {

		VerificationToken verificationToken = tokenRepository.findByTokenAndTokenType(token, TokenType.EMAIL_VERIFY)
				.orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

		if (verificationToken.getIsUsed()) {
			throw new InvalidTokenException("This token has already been used");
		}

		if (verificationToken.isExpired()) {
			throw new InvalidTokenException("Verification token has expired");
		}

		// Mark token used
		verificationToken.setIsUsed(true);
		tokenRepository.save(verificationToken);

		// Mark user verified
		User user = verificationToken.getUser();
		user.setIsVerified(true);
		userRepository.save(user);

		log.info("Email verified for user: {}", user.getEmail());
	}

	// ─────────────────────────────────────────
	// FORGOT PASSWORD
	// ─────────────────────────────────────────
	@Override
	@Transactional
	public void sendPasswordResetEmail(String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

		// Invalidate old reset tokens
		tokenRepository.findAllByUserAndTokenType(user, TokenType.PASSWORD_RESET).forEach(t -> {
			t.setIsUsed(true);
			tokenRepository.save(t);
		});

		// Create new reset token
		String token = UUID.randomUUID().toString();

		VerificationToken resetToken = new VerificationToken();
		resetToken.setToken(token);
		resetToken.setTokenType(TokenType.PASSWORD_RESET);
		resetToken.setUser(user);
		resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		resetToken.setIsUsed(false);

		tokenRepository.save(resetToken);

		// Send email
		String resetLink = frontendUrl + "/reset-password?token=" + token;

		emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);

		log.info("Password reset email sent to: {}", email);
	}

	// ─────────────────────────────────────────
	// RESET PASSWORD
	// ─────────────────────────────────────────
	@Override
	@Transactional
	public void resetPassword(String token, String newPassword) {

		VerificationToken resetToken = tokenRepository.findByTokenAndTokenType(token, TokenType.PASSWORD_RESET)
				.orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

		if (resetToken.getIsUsed()) {
			throw new InvalidTokenException("This reset link has already been used");
		}

		if (resetToken.isExpired()) {
			throw new InvalidTokenException("Reset token has expired. Please request a new one");
		}

		// Update password
		User user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		resetToken.setIsUsed(true);
		tokenRepository.save(resetToken);

		log.info("Password reset for user: {}", user.getEmail());
	}

	// ─────────────────────────────────────────
	// REFRESH TOKEN
	// ─────────────────────────────────────────
	@Override
	@Transactional
	public AuthResponse refreshToken(String refreshToken) {

		RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
				.orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

		if (tokenEntity.getIsRevoked()) {
			throw new InvalidTokenException("Refresh token has been revoked");
		}

		if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new InvalidTokenException("Refresh token has expired. Please login again");
		}

		User user = tokenEntity.getUser();

		// Revoke old refresh token
		tokenEntity.setIsRevoked(true);
		refreshTokenRepository.save(tokenEntity);

		// Generate new tokens
		String newAccessToken = jwtTokenProvider.generateAccessToken(user);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

		// Save new refresh token
		RefreshToken newTokenEntity = new RefreshToken();
		newTokenEntity.setToken(newRefreshToken);
		newTokenEntity.setUser(user);
		newTokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
		newTokenEntity.setIsRevoked(false);

		refreshTokenRepository.save(newTokenEntity);

		return AuthResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).tokenType("Bearer")
				.userId(user.getId()).email(user.getEmail()).fullName(user.getFullName()).role(user.getRole().name())
				.isVerified(user.getIsVerified()).build();
	}

	// ─────────────────────────────────────────
	// LOGOUT
	// ─────────────────────────────────────────
	@Override
	@Transactional
	public void logout(String refreshToken) {

		RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
				.orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

		tokenEntity.setIsRevoked(true);
		refreshTokenRepository.save(tokenEntity);

		log.info("User logged out, refresh token revoked");
	}
}