package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sesion_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    /** SHA-256 (hex) del JWT; nunca se guarda el token en claro. */
    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @Column(name = "revocada_en")
    private Instant revocadaEn;

    @PrePersist
    void prePersist() {
        if (creadaEn == null) {
            creadaEn = Instant.now();
        }
    }

    public boolean estaActiva() {
        return revocadaEn == null && expiraEn.isAfter(Instant.now());
    }
}
