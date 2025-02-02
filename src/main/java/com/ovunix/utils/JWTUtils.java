package com.ovunix.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTUtils {

    @Value("${app.secret-key}")
    private String secretKey;

    @Value("${app.pub-key}")
    private String publicKeyBase64;



    @Value("${app-expiration-time}")
    private long expirateTime;

    public String generateToken(String username){
        Map<String,Object> claims=new HashMap<>();
        claims.put("type", "access");
        return createToken(claims,username);
    }


    private String  createToken (Map<String,Object> claims, String subject){
        return Jwts.builder().setClaims(claims).setSubject(subject).
                setIssuedAt(new Date(System.currentTimeMillis())).
                setExpiration(new Date(System.currentTimeMillis()+expirateTime))
                .signWith(getSignKey(), SignatureAlgorithm.ES256)
                .compact();
    }


    private PrivateKey getSignKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la clé privée", e);
        }
    }


    public Boolean validateToken (String token, UserDetails userDetails){
      String username=extractUsername(token);
      return (username.equals(userDetails.getUsername())) && !isTokenExppired (token);
    }



    public boolean isTokenExppired(String token) {
        return extractExpiredDate(token).before(new Date());
    }

    private Date extractExpiredDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver) {
        final Claims claims=extractAllClaims (token);
        return claimsResolver.apply(claims);
    }

    private PublicKey getSignKeyVefication() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la clé publique", e);
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSignKeyVefication()).build().parseSignedClaims(token).getPayload();
    }

    public String createRefreshToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        long refreshTokenExpirationTime = expirateTime*2;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(getSignKey(), SignatureAlgorithm.ES256)
                .compact();
    }


    public String generateAccessTokenFromRefreshToken(String refreshToken) {

        if (!isValidRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh Token invalide ou expiré");
        }

        // Extraire le sujet (username) du Refresh Token
        String subject = extractUsername(refreshToken);

        // Générer un nouvel Access Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");

        return createToken(claims, subject);
    }


    private boolean isValidRefreshToken(String refreshToken) {
        try {

            Claims claims = extractAllClaims(refreshToken);

            if (!"refresh".equals(claims.get("type"))) {
                return false;
            }

            // Vérifier que le token n'a pas expiré
            return !isTokenExppired(refreshToken);
        } catch (Exception e) {
            return false;
        }
    }
}
