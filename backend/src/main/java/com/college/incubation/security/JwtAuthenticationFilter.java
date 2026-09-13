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
        // 0. CORS PREFLIGHT
        // ==========================================

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 1. GET AUTHORIZATION HEADER
        // ==========================================

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 2. EXTRACT TOKEN
        // ==========================================

        String token =
                authorizationHeader
                        .substring(7)
                        .trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 3. VALIDATE TOKEN
        // ==========================================

        if (!jwtUtils.isValid(token)) {

            System.out.println(
                    "JWT DEBUG -> Invalid JWT token"
            );

            filterChain.doFilter(request, response);
            return;
        }

        try {

            // ==========================================
            // 4. EXTRACT CLAIMS
            // ==========================================

            Claims claims =
                    jwtUtils.extractClaims(token);

            // ==========================================
            // 5. EMAIL
            // ==========================================

            String email =
                    claims.get(
                            "email",
                            String.class
                    );

            // ==========================================
            // 6. ROLE
            // ==========================================

            String role =
                    claims.get(
                            "role",
                            String.class
                    );

            // ==========================================
            // 7. VALIDATE EMAIL
            // ==========================================

            if (email == null ||
                    email.isBlank()) {

                System.out.println(
                        "JWT DEBUG -> Email missing"
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            // ==========================================
            // 8. VALIDATE ROLE
            // ==========================================

            if (role == null ||
                    role.isBlank()) {

                System.out.println(
                        "JWT DEBUG -> Role missing for "
                                + email
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            // ==========================================
            // 9. NORMALIZE ROLE
            // ==========================================

            role =
                    role
                            .trim()
                            .toUpperCase();

            // If JWT contains ROLE_STUDENT
            // convert it to STUDENT

            if (role.startsWith("ROLE_")) {

                role =
                        role.substring(5);
            }

            // ==========================================
            // 10. CREATE SPRING AUTHORITY
            // ==========================================

            String authorityName =
                    "ROLE_" + role;

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            authorityName
                    );

            // ==========================================
            // 11. DEBUG
            // ==========================================

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "JWT DEBUG -> Email      : "
                            + email
            );

            System.out.println(
                    "JWT DEBUG -> JWT Role   : "
                            + role
            );

            System.out.println(
                    "JWT DEBUG -> Authority  : "
                            + authorityName
            );

            System.out.println(
                    "JWT DEBUG -> Request    : "
                            + request.getMethod()
                            + " "
                            + request.getRequestURI()
            );

            System.out.println(
                    "=========================================="
            );

            // ==========================================
            // 12. CREATE AUTHENTICATION
            // ==========================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );

            // ==========================================
            // 13. SET SECURITY CONTEXT
            // ==========================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (Exception e) {

            SecurityContextHolder
                    .clearContext();

            System.out.println(
                    "JWT Authentication failed: "
                            + e.getMessage()
            );
        }

        // ==========================================
        // 14. CONTINUE REQUEST
        // ==========================================

        filterChain.doFilter(
                request,
                response
        );
    }
}