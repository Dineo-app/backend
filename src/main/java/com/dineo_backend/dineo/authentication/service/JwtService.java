package com.dineo_backend.dineo.authentication.service;

import com.dineo_backend.dineo.authentication.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT utility service for generating and validating JWT tokens
 */
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    
    /**
     * Generate access token for user
     */
    public String generateAccessToken(UUID userId, String email, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "ACCESS");
        
        // Use userId as subject instead of email
        return createToken(claims, userId.toString(), accessTokenExpiration);
    }
    
    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(UUID userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");
        
        // Use userId as subject instead of email
        return createToken(claims, userId.toString(), refreshTokenExpiration);
    }
    
    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Extract user ID from token (now stored in subject)
     */
    public String extractUsername(String token) {
        // Return the user ID since that's what's stored in subject now
        return extractAllClaims(token).getSubject();
    }
    
    /**
     * Extract user ID from token
     */
    public UUID extractUserId(String token) {
        // User ID is now stored in the subject
        String userIdString = extractAllClaims(token).getSubject();
        return UUID.fromString(userIdString);
    }
    
    /**
     * Extract role from token - No longer available in token
     * This method is kept for backward compatibility but will return null
     */
    @Deprecated
    public Role extractRole(String token) {
        // Role is no longer stored in tokens
        return null;
    }
    
    /**
     * Extract token type from token
     */
    public String extractTokenType(String token) {
        return (String) extractAllClaims(token).get("tokenType");
    }
    
    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
    
    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Validate token
     */
    public Boolean validateToken(String token, String userId) {
        try {
            final String extractedUserId = extractUsername(token); // This returns user ID now
            return (extractedUserId.equals(userId) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate token by user ID
     */
    public Boolean validateToken(String token, UUID userId) {
        return validateToken(token, userId.toString());
    }
    
    /**
     * Check if token is access token
     */
    public Boolean isAccessToken(String token) {
        try {
            return "ACCESS".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if token is refresh token
     */
    public Boolean isRefreshToken(String token) {
        try {
            return "REFRESH".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
}