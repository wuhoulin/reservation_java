package com.microservice.skeleton.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:defaultSecretKeyThatIsLongEnoughForHS512Algorithm}")
    private String secret;

    @Value("${jwt.expiration:7200}")
    private Long expiration;

    // 使用HS256算法，对密钥长度要求较低
    public String generateToken(String openid, Map<String, Object> claims) {
        if (claims == null) {
            claims = new HashMap<>();
        }

        // 使用Base64编码的密钥
        String base64Secret = Base64.getEncoder().encodeToString(secret.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(openid)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS256, base64Secret) // 使用HS256
                .compact();
    }

    public String getOpenidFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        String base64Secret = Base64.getEncoder().encodeToString(secret.getBytes());
        return Jwts.parser()
                .setSigningKey(base64Secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, String openid) {
        final String tokenOpenid = getOpenidFromToken(token);
        return (tokenOpenid.equals(openid) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            final String openid = getOpenidFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + expiration * 1000));

        String base64Secret = Base64.getEncoder().encodeToString(secret.getBytes());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, base64Secret)
                .compact();
    }

    public Long getRemainingTime(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        final Date now = new Date();
        return (expiration.getTime() - now.getTime()) / 1000;
    }
}
