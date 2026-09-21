package com.example.demo.dto.response;

public class AuthResponse {

	private String accessToken;
	private String refreshToken;
	private String tokenType = "Bearer";
	private Long userId;
	private String fullName;
	private String email;
	private String role;
	private Boolean isVerified;

	// ─── Private constructor (used by Builder only) ───
	private AuthResponse() {}

	// ─────────────────────────────────────────────────
	// BUILDER
	// ─────────────────────────────────────────────────
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String accessToken;
		private String refreshToken;
		private String tokenType = "Bearer";
		private Long userId;
		private String fullName;
		private String email;
		private String role;
		private Boolean isVerified;

		public Builder accessToken(String accessToken) {
			this.accessToken = accessToken;
			return this;
		}

		public Builder refreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
			return this;
		}

		public Builder tokenType(String tokenType) {
			this.tokenType = tokenType;
			return this;
		}

		public Builder userId(Long userId) {
			this.userId = userId;
			return this;
		}

		public Builder fullName(String fullName) {
			this.fullName = fullName;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder role(String role) {
			this.role = role;
			return this;
		}

		public Builder isVerified(Boolean isVerified) {
			this.isVerified = isVerified;
			return this;
		}

		public AuthResponse build() {
			AuthResponse response = new AuthResponse();
			response.accessToken  = this.accessToken;
			response.refreshToken = this.refreshToken;
			response.tokenType    = this.tokenType;
			response.userId       = this.userId;
			response.fullName     = this.fullName;
			response.email        = this.email;
			response.role         = this.role;
			response.isVerified   = this.isVerified;
			return response;
		}
	}

	// ─────────────────────────────────────────────────
	// GETTERS & SETTERS
	// ─────────────────────────────────────────────────
	public String getAccessToken() { return accessToken; }
	public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

	public String getRefreshToken() { return refreshToken; }
	public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

	public String getTokenType() { return tokenType; }
	public void setTokenType(String tokenType) { this.tokenType = tokenType; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }

	public Boolean getIsVerified() { return isVerified; }
	public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
}