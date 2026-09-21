package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public class JwtUtils {

    // ─────────────────────────────────────────
    // PREVENT INSTANTIATION
    // Utility class — all methods are static
    // ─────────────────────────────────────────
    private JwtUtils() {}

    private static final Logger log =
        LoggerFactory.getLogger(JwtUtils.class);

    // ─────────────────────────────────────────
    // BUILD SIGNING KEY FROM SECRET
    // ─────────────────────────────────────────
    public static SecretKey buildSigningKey(String secret) {
        return Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ─────────────────────────────────────────
    // GENERATE TOKEN WITH CUSTOM CLAIMS
    // ─────────────────────────────────────────
    public static String generateToken(String subject,
                                       Map<String, Object> claims,
                                       long expiryMillis,
                                       String secret) {
        Date now        = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMillis);

        var builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(buildSigningKey(secret),
                          SignatureAlgorithm.HS256);

        // Add all custom claims
        if (claims != null && !claims.isEmpty()) {
            claims.forEach(builder::claim);
        }

        return builder.compact();
    }

    // ─────────────────────────────────────────
    // EXTRACT ALL CLAIMS FROM TOKEN
    // ─────────────────────────────────────────
    public static Claims extractAllClaims(String token,
                                          String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(buildSigningKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ─────────────────────────────────────────
    // EXTRACT SINGLE CLAIM
    // ─────────────────────────────────────────
    public static <T> T extractClaim(String token,
                                     String secret,
                                     Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token, secret);
        return claimsResolver.apply(claims);
    }

    // ─────────────────────────────────────────
    // EXTRACT SUBJECT (email)
    // ─────────────────────────────────────────
    public static String extractSubject(String token,
                                        String secret) {
        return extractClaim(token, secret, Claims::getSubject);
    }

    // ─────────────────────────────────────────
    // EXTRACT EXPIRATION DATE
    // ─────────────────────────────────────────
    public static Date extractExpiration(String token,
                                         String secret) {
        return extractClaim(token, secret, Claims::getExpiration);
    }

    // ─────────────────────────────────────────
    // EXTRACT ISSUED AT DATE
    // ─────────────────────────────────────────
    public static Date extractIssuedAt(String token,
                                       String secret) {
        return extractClaim(token, secret, Claims::getIssuedAt);
    }

    // ─────────────────────────────────────────
    // EXTRACT CUSTOM CLAIM BY KEY
    // ─────────────────────────────────────────
    public static String extractClaimAsString(String token,
                                              String secret,
                                              String claimKey) {
        return extractAllClaims(token, secret)
                .get(claimKey, String.class);
    }

    public static Long extractClaimAsLong(String token,
                                          String secret,
                                          String claimKey) {
        return extractAllClaims(token, secret)
                .get(claimKey, Long.class);
    }

    public static Integer extractClaimAsInteger(String token,
                                                String secret,
                                                String claimKey) {
        return extractAllClaims(token, secret)
                .get(claimKey, Integer.class);
    }

    // ─────────────────────────────────────────
    // EXTRACT ROLE FROM TOKEN
    // ─────────────────────────────────────────
    public static String extractRole(String token, String secret) {
        return extractClaimAsString(token, secret, "role");
    }

    // ─────────────────────────────────────────
    // EXTRACT USER ID FROM TOKEN
    // ─────────────────────────────────────────
    public static Long extractUserId(String token, String secret) {
        return extractClaimAsLong(token, secret, "userId");
    }

    // ─────────────────────────────────────────
    // EXTRACT FULL NAME FROM TOKEN
    // ─────────────────────────────────────────
    public static String extractFullName(String token,
                                         String secret) {
        return extractClaimAsString(token, secret, "fullName");
    }

    // ─────────────────────────────────────────
    // CHECK IF TOKEN IS EXPIRED
    // ─────────────────────────────────────────
    public static boolean isTokenExpired(String token,
                                         String secret) {
        try {
            return extractExpiration(token, secret)
                    .before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // ─────────────────────────────────────────
    // VALIDATE TOKEN — FULL VALIDATION
    // Returns reason string or null if valid
    // ─────────────────────────────────────────
    public static boolean validateToken(String token,
                                        String secret) {
        try {
            extractAllClaims(token, secret);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT Token expired: {}", e.getMessage());
            return false;

        } catch (UnsupportedJwtException e) {
            log.warn("JWT Token unsupported: {}", e.getMessage());
            return false;

        } catch (MalformedJwtException e) {
            log.warn("JWT Token malformed: {}", e.getMessage());
            return false;

        } catch (SignatureException e) {
            log.warn("JWT Signature invalid: {}", e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            log.warn("JWT Token empty or null: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            log.warn("JWT Token validation error: {}", e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // GET VALIDATION FAILURE REASON
    // Useful for detailed error responses
    // ─────────────────────────────────────────
    public static String getValidationFailureReason(String token,
                                                     String secret) {
        try {
            extractAllClaims(token, secret);
            return null; // valid — no failure reason

        } catch (ExpiredJwtException e) {
            return "TOKEN_EXPIRED";

        } catch (UnsupportedJwtException e) {
            return "TOKEN_UNSUPPORTED";

        } catch (MalformedJwtException e) {
            return "TOKEN_MALFORMED";

        } catch (SignatureException e) {
            return "SIGNATURE_INVALID";

        } catch (IllegalArgumentException e) {
            return "TOKEN_EMPTY";

        } catch (Exception e) {
            return "TOKEN_INVALID";
        }
    }

    // ─────────────────────────────────────────
    // GET REMAINING EXPIRY IN MILLISECONDS
    // ─────────────────────────────────────────
    public static long getRemainingExpiryMillis(String token,
                                                String secret) {
        try {
            Date expiration = extractExpiration(token, secret);
            long remaining  = expiration.getTime()
                              - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    // ─────────────────────────────────────────
    // GET REMAINING EXPIRY IN MINUTES
    // ─────────────────────────────────────────
    public static long getRemainingExpiryMinutes(String token,
                                                  String secret) {
        return getRemainingExpiryMillis(token, secret) / 60000;
    }

    // ─────────────────────────────────────────
    // CHECK IF TOKEN IS ABOUT TO EXPIRE
    // (within given threshold in minutes)
    // ─────────────────────────────────────────
    public static boolean isTokenAboutToExpire(String token,
                                               String secret,
                                               int thresholdMinutes) {
        long remainingMinutes = getRemainingExpiryMinutes(token, secret);
        return remainingMinutes <= thresholdMinutes;
    }

    // ─────────────────────────────────────────
    // EXTRACT TOKEN FROM BEARER STRING
    // "Bearer eyJhbGc..." → "eyJhbGc..."
    // ─────────────────────────────────────────
    public static String extractTokenFromBearer(String bearerToken) {
        if (bearerToken != null &&
            bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // ─────────────────────────────────────────
    // CHECK IF STRING IS BEARER TOKEN FORMAT
    // ─────────────────────────────────────────
    public static boolean isBearerToken(String authHeader) {
        return authHeader != null &&
               authHeader.startsWith("Bearer ") &&
               authHeader.length() > 7;
    }
}