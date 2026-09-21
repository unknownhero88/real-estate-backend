package com.example.demo.config;

import com.example.demo.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.config.CorsConfig;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfig corsConfig;              // ← inject CorsConfig

    // ✅ Manual constructor
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          CustomUserDetailsService userDetailsService,
                          CorsConfig corsConfig,
                            RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter      = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.corsConfig         = corsConfig;
        this.rateLimitFilter = rateLimitFilter;

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // === Public Auth & Static ===
                                "/api/auth/**",
                                "/uploads/**",
                                "/ws/**",
                                "/api/auctions/**",

                                // === Public Property Endpoints ===
                                "/api/properties",
                                "/api/properties/search",
                                "/api/properties/{id}",
                                "/api/properties/{id}/images",
                                "/api/properties/*/pdf",

                                // === Phase 5 Public ===
                                "/api/contact",
                                "/api/newsletter/subscribe",
                                "/api/newsletter/unsubscribe",

                                // === Phase 6 Public ===
                                "/api/recommendations/similar/**",     // Similar properties
                                "/api/agents",                         // All verified agents
                                "/api/agents/{id}",                    // Single agent
                                "/api/v2/properties/**",               // API v2

                                "/api/trending/**",

                                // === Swagger ===
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                            "/actuator/health",
                                "/webjars/**"
                        ).permitAll()

                        // Role-based protected routes
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/seller/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/buyer/**").hasAnyRole("BUYER", "ADMIN")

                        // Authenticated but not role-specific
                        .requestMatchers(
                                "/api/recommendations/for-me",      // Personalized recommendations
                                "/api/agents/my-profile",           // Seller creates agent profile
                                "/api/chat/**",
                                "/api/payments/**",
                                "/api/appointments/**",
                                "/api/wishlist/**",
                                "/api/reviews/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Rate Limiting Filter (Phase 7)
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory() {
        return handler -> new WebSocketHandlerDecorator(handler) {
            // Optional: log connections
        };
    }


    }
