package vn.angi.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.angi.config.AppProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppProperties appProperties;

    public String generateAccessToken(UUID userId, String email) {
        long ttlSeconds = appProperties.getJwt().getAccessTokenTtl();
        return Jwts.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlSeconds * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        long ttlSeconds = appProperties.getJwt().getRefreshTokenTtl();
        return Jwts.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlSeconds * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenTtlSeconds() {
        return appProperties.getJwt().getAccessTokenTtl();
    }

    public long getRefreshTokenTtlSeconds() {
        return appProperties.getJwt().getRefreshTokenTtl();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
