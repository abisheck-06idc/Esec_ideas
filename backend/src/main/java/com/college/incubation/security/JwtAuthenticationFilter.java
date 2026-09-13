package com.college.incubation.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ==========================================
        // 0. CORS PREFLIGHT REQUEST
        // ==========================================
        // Browser sends OPTIONS request before POST login.
        // JWT authentication must NOT be applied to OPTIONS.

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 1. Get Authorization Header
        // ==========================================

        String authorizationHeader =
                request.getHeader("Authorization");

        // No Authorization header
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 2. Extract JWT Token
        // ==========================================

        String token =
                authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 3. Validate JWT
        // ==========================================

        if (!jwtUtils.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // ==========================================
            // 4. Extract Claims
            // ==========================================

            Claims claims =
                    jwtUtils.extractClaims(token);

            // ==========================================
            // 5. Get Email
            // ==========================================

            String email =
                    claims.get(
                            "email",
                            String.class
                    );

            // ==========================================
            // 6. Get Role
            // ==========================================

            String role =
                    claims.get(
                            "role",
                            String.class
                    );

            // ==========================================
            // 7. Validate Claims
            // ==========================================

            if (email == null || email.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (role == null || role.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            // ==========================================
            // 8. Normalize Role
            // ==========================================

            role = role.trim().toUpperCase();

            // Remove ROLE_ if JWT already contains it
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }

            // ==========================================
            // 9. Create Authority
            // ==========================================

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + role
                    );

            // ==========================================
            // 10. Create Authentication
            // ==========================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );

            // ==========================================
            // 11. Store Authentication
            // ==========================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {

            SecurityContextHolder
                    .clearContext();

            System.out.println(
                    "JWT Authentication failed: "
                            + e.getMessage()
            );
        }

        // ==========================================
        // 12. Continue Request
        // ==========================================

        filterChain.doFilter(
                request,
                response
        );
    }
}