package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "asistencia_clase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaClaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "socio_clase_id", nullable = false)
    private SocioClaseEntity socioClase;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAsistencia estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registrado_por", nullable = false)
    private EmpleadoEntity registradoPor;

    @Column(name = "registrado_en", nullable = false)
    private Instant registradoEn;

    @Column(length = 300)
    private String observacion;

    @PrePersist
    void prePersist() {
        if (registradoEn == null) {
            registradoEn = Instant.now();
        }
    }
}
