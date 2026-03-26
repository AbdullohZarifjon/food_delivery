package com.example.food.security;

import com.example.food.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.access.token.secretKey}")
    private String accessSecretKey;

    @Value("${jwt.access.token.expire.date}")
    private Long accessExpiration;

    @Value("${jwt.refresh.token.secretKey}")
    private String refreshSecretKey;

    @Value("${jwt.refresh.token.expire.date}")
    private Long refreshExpiration;

    public String generateAccessToken(User user) {
        return buildToken(new HashMap<>(), user, accessExpiration, getAccessKey());
    }

    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpiration, getRefreshKey());
    }

    private String buildToken(Map<String, Object> extraClaims, User user, long expiration, SecretKey key) {
        if (expiration == accessExpiration) {
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            extraClaims.put("roles", roles);
        }

        // 2. Ism va Telefon raqamini qo'shimcha claim sifatida qo'shamiz
        extraClaims.put("id", user.getId());
        extraClaims.put("name", user.getName());
        extraClaims.put("phone", user.getPhoneNumber());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername()) // Username odatda telefon raqami bo'ladi
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    // Access tokenni o'qish uchun (Filterda ishlatiladi)
    public Claims extractAccessClaims(String token) {
        return Jwts.parser()
                .verifyWith(getAccessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Refresh tokenni o'qish uchun (Token yangilashda ishlatiladi)
    public Claims extractRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(getRefreshKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
    }
}