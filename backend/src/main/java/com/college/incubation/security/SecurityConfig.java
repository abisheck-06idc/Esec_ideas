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

import java.util.ArrayList;
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

                        // Allow CORS preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Health + Login / Register
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // Admin APIs
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // Authenticated users
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

        List<String> allowedOrigins = new ArrayList<>(List.of(
                "https://esec-ideas.vercel.app",
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost",
                "http://127.0.0.1",
                "https://victor-solve-parker-quad.trycloudflare.com"
        ));

        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            allowedOrigins.add(frontendUrl.trim().replaceAll("/$", ""));
        }

        configuration.setAllowedOrigins(allowedOrigins);

        // ==========================================
        // ALLOWED METHODS
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
                List.of("*")
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
        // CORS CONFIGURATION SOURCE
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