package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.util.JwtUtils;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
        LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider        jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider  = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ✅ Use JwtUtils to check bearer format
        if (!JwtUtils.isBearerToken(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Use JwtUtils to extract token
        String token = JwtUtils.extractTokenFromBearer(authHeader);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.extractEmail(token);

            UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            authToken.setDetails(
                new WebAuthenticationDetailsSource()
                    .buildDetails(request));

            SecurityContextHolder.getContext()
                .setAuthentication(authToken);

            log.debug("Authenticated user: {}, role: {}",
                email,
                jwtTokenProvider.extractRole(token));
        }

        filterChain.doFilter(request, response);
    }
}