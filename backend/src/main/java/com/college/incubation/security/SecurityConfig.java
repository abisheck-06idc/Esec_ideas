package com.college.incubation.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // ==========================================
                // CORS
                // ==========================================
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                // ==========================================
                // CSRF
                // ==========================================
                .csrf(csrf -> csrf.disable())

                // ==========================================
                // STATELESS JWT
                // ==========================================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ==========================================
                // AUTHORIZATION
                // ==========================================
                .authorizeHttpRequests(auth -> auth

                        // CORS Preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Login / Register
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // Admin APIs
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // Student / Authenticated users
                        .requestMatchers(
                                "/api/ideas/**"
                        ).authenticated()

                        // Everything else
                        .anyRequest().authenticated()
                )

                // ==========================================
                // JWT FILTER
                // ==========================================
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ==========================================
    // CORS CONFIGURATION
    // ==========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // ==========================================
        // ALLOWED ORIGINS
        // ==========================================
        configuration.setAllowedOrigins(
                List.of(
                        // Local development
                        "http://localhost:5500",
                        "http://127.0.0.1:5500",
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost",
                        "http://127.0.0.1",

                        // Vercel Frontend
                        "https://esec-ideas.vercel.app",

                        // Cloudflare Tunnel
                        "https://victor-solve-parker-quad.trycloudflare.com"
                )
        );

        // ==========================================
        // ALLOWED HTTP METHODS
        // ==========================================
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        // ==========================================
        // ALLOWED HEADERS
        // ==========================================
        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );

        // ==========================================
        // EXPOSED HEADERS
        // ==========================================
        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        // ==========================================
        // CREDENTIALS
        // ==========================================
        configuration.setAllowCredentials(true);

        // ==========================================
        // REGISTER CORS CONFIGURATION
        // ==========================================
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}