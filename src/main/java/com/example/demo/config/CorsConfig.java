package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ─────────────────────────────────────────
        // ALLOWED ORIGINS (React frontend URLs)
        // ─────────────────────────────────────────
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",   // Vite dev server
            "http://localhost:3000",   // fallback
            "http://localhost:4173",   // Vite preview
                "http://localhost:5174",
                "http://192.168.1.50:5173"
        ));

        // ─────────────────────────────────────────
        // ALLOWED HTTP METHODS
        // ─────────────────────────────────────────
        config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        // ─────────────────────────────────────────
        // ALLOWED HEADERS
        // ─────────────────────────────────────────
        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // ─────────────────────────────────────────
        // EXPOSED HEADERS
        // Headers frontend can read from response
        // ─────────────────────────────────────────
        config.setExposedHeaders(List.of(
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));

        // ─────────────────────────────────────────
        // ALLOW CREDENTIALS (cookies, auth headers)
        // ─────────────────────────────────────────
        config.setAllowCredentials(true);

        // ─────────────────────────────────────────
        // PREFLIGHT CACHE DURATION (1 hour)
        // Browser won't send OPTIONS request again
        // for 1 hour for same endpoint
        // ─────────────────────────────────────────
        config.setMaxAge(3600L);

        // Apply config to ALL endpoints
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}