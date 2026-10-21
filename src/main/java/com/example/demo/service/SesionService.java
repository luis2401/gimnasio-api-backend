package com.example.demo.service;

import com.example.demo.Model.SesionModel;
import com.example.demo.entity.SesionUsuarioEntity;
import com.example.demo.entity.UsuarioEntity;
import com.example.demo.repository.SesionUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;

/**
 * Gestiona las sesiones (tabla sesion_usuario). Cada JWT emitido se registra con su hash; al cerrar
 * sesión o al bloquear/desactivar al usuario la sesión se revoca y el token deja de ser válido.
 */
@Service
public class SesionService {

    @Autowired
    private SesionUsuarioRepository sesionRepository;

    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    @Transactional
    public void registrar(UsuarioEntity usuario, String token, Date expiracion) {
        Instant ahora = Instant.now();
        Instant expira = expiracion.toInstant();
        if (!expira.isAfter(ahora)) {
            expira = ahora.plusSeconds(1);
        }
        sesionRepository.save(SesionUsuarioEntity.builder()
                .usuario(usuario)
                .tokenHash(hash(token))
                .creadaEn(ahora)
                .expiraEn(expira)
                .build());
    }

    /** true si existe una sesión no revocada ni expirada para ese token. */
    @Transactional(readOnly = true)
    public boolean esSesionActiva(String token) {
        return sesionRepository.findByTokenHash(hash(token))
                .map(SesionUsuarioEntity::estaActiva)
                .orElse(false);
    }

    @Transactional
    public void revocar(String token) {
        sesionRepository.findByTokenHash(hash(token)).ifPresent(s -> {
            if (s.getRevocadaEn() == null) {
                s.setRevocadaEn(Instant.now());
            }
        });
    }

    @Transactional
    public int revocarTodas(Long usuarioId) {
        return sesionRepository.revocarTodasDeUsuario(usuarioId, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<SesionModel> listarActivas(Long usuarioId) {
        return sesionRepository.findActivasByUsuarioId(usuarioId, Instant.now()).stream()
                .map(s -> SesionModel.builder()
                        .id(s.getId())
                        .usuarioId(usuarioId)
                        .creadaEn(s.getCreadaEn())
                        .expiraEn(s.getExpiraEn())
                        .revocadaEn(s.getRevocadaEn())
                        .activa(true)
                        .build())
                .toList();
    }

    /** Limpieza diaria de sesiones expiradas hace más de 7 días. */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void limpiarExpiradas() {
        sesionRepository.eliminarExpiradasAntesDe(Instant.now().minus(7, ChronoUnit.DAYS));
    }
}
