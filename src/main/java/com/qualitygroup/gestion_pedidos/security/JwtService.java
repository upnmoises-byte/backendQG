package com.qualitygroup.gestion_pedidos.security;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        byte[] keyBytes = secret.length() >= 64
                ? Decoders.BASE64.decode(secret)
                : padSecret(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    private static byte[] padSecret(String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 32) {
            return raw;
        }
        byte[] padded = new byte[32];
        System.arraycopy(raw, 0, padded, 0, raw.length);
        for (int i = raw.length; i < 32; i++) {
            padded[i] = (byte) (i % 256);
        }
        return padded;
    }

    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol())
                .claim("nombre", usuario.getNombre())
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
        Claims claims = parseClaims(token);
        String rol = claims.get("rol", String.class);
        if (rol == null || rol.isBlank()) {
            return List.of();
        }
        String rolNorm = rol.trim().toUpperCase();
        return List.of(new SimpleGrantedAuthority("ROLE_" + rolNorm));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
