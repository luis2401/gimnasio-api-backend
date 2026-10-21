package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "socio_clase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocioClaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "socio_id", nullable = false)
    private SocioEntity socio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clase_id", nullable = false)
    private ClaseEntity clase;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDate fechaInscripcion;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @PrePersist
    void prePersist() {
        if (fechaInscripcion == null) {
            fechaInscripcion = LocalDate.now();
        }
    }
}
