package com.college.incubation.security;

import com.college.incubation.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private final SecretKey key;

    private final long expiration;

    public JwtUtils(
            @Value("${app.jwt.secret}")
            String secret,

            @Value("${app.jwt.expiration}")
            long expiration
    ) {

        this.key =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        this.expiration = expiration;
    }

    public String generateToken(
            User user
    ) {

        Date now =
                new Date();

        return Jwts.builder()
                .subject(user.getEmail())

                .claim(
                        "id",
                        user.getId().toString()
                )

                .claim(
                        "email",
                        user.getEmail()
                )

                .claim(
                        "role",
                        user.getRole().name()
                )

                .issuedAt(now)

                .expiration(
                        new Date(
                                now.getTime()
                                + expiration
                        )
                )

                .signWith(key)

                .compact();
    }

    public Claims extractClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(
            String token
    ) {

        return UUID.fromString(
                extractClaims(token)
                        .get(
                                "id",
                                String.class
                        )
        );
    }

    public String extractEmail(
            String token
    ) {

        return extractClaims(token)
                .get(
                        "email",
                        String.class
                );
    }

    public String extractRole(
            String token
    ) {

        return extractClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public boolean isValid(
            String token
    ) {

        try {

            extractClaims(token);

            return true;

        } catch (
                JwtException |
                IllegalArgumentException e
        ) {

            return false;
        }
    }
}