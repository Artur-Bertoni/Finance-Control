package com.financecontrol.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

@Component
@SuppressWarnings("null")
public class JwtUtil {

    public static final String COOKIE_NAME = "auth_token";

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-days:7}") int expirationDays) {
        if (secret == null || secret.isBlank())
            throw new IllegalStateException("app.jwt.secret (env JWT_SECRET) é obrigatório e não pode ser vazio");
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalStateException("app.jwt.secret (env JWT_SECRET) deve ter no mínimo 32 bytes");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = (long) expirationDays * 24 * 60 * 60 * 1000;
    }

    public String generateToken(Long userId) {
        long nowSeconds = Instant.now().getEpochSecond();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("iat", nowSeconds)
                .claim("exp", nowSeconds + expirationMs / 1000)
                .signWith(key)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void setTokenCookie(HttpServletResponse response,
                               String token, 
                               boolean secure) {
        String cookieValue = COOKIE_NAME + "=" + token
                + "; Path=/"
                + "; HttpOnly"
                + "; SameSite=Lax"
                + "; Max-Age=" + (expirationMs / 1000)
                + (secure ? "; Secure" : "");

        response.addHeader("Set-Cookie", cookieValue);
    }

    public void clearTokenCookie(HttpServletResponse response) {
        String cookieValue = COOKIE_NAME + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0";
        response.addHeader("Set-Cookie", cookieValue);
    }

    public String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer "))
            return header.substring(7).trim();
        return null;
    }

    public String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
