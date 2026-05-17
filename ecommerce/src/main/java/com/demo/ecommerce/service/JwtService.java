package com.demo.ecommerce.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// Import required annotations and add business logics
@Service
public class JwtService {

    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347";
    public static final long JWT_TOKEN_VALIDITY = 900000;
    public String extractUsername(String token) {
        return extractClaim(token, Claims :: getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims :: getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    }

    private Boolean isTokenExpired(String token) {
         return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        return (extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    public String generateToken(String userName){
        return createToken(new HashMap<>(), userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSignKey())
                    .setSubject(userName)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                    .compact();
    }

    private Key getSignKey() {
        final var key = Decoders.BASE64URL.decode(SECRET);
        return Keys.hmacShaKeyFor(key);
    }
}
