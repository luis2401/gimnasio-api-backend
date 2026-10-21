package com.example.demo.security;

import com.example.demo.entity.UsuarioEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(UsuarioEntity usuario) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRolCodigo())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(clave())
                .compact();
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(clave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerUsername(String token) {
        return claims(token).getSubject();
    }

    /** Identificador único del token; los tokens emitidos antes de existir sesiones no lo tienen (null). */
    public String extraerJti(String token) {
        return claims(token).getId();
    }

    public Date extraerExpiracion(String token) {
        return claims(token).getExpiration();
    }

    public boolean esValido(String token) {
        try {
            claims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}