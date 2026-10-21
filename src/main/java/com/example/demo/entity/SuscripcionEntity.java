package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "suscripcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuscripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "socio_id", nullable = false)
    private SocioEntity socio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_sede_id", nullable = false)
    private PlanSedeEntity planSede;

    @Column(name = "nombre_plan_vendido", nullable = false, length = 120)
    private String nombrePlanVendido;

    @Column(name = "duracion_dias_vendida", nullable = false)
    private Integer duracionDiasVendida;

    @Column(name = "precio_vendido", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVendido;

    @Column(name = "precio_lista", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioLista;

    @Column(name = "condiciones_vendidas", length = 1000)
    private String condicionesVendidas;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoSuscripcion estado = EstadoSuscripcion.ACTIVA;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Column(name = "observacion_precio", length = 500)
    private String observacionPrecio;

    @Column(name = "acceso_multisede", nullable = false)
    private boolean accesoMultisede;

    @Column(name = "recargo_multisede", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal recargoMultisede = BigDecimal.ZERO;

    @PrePersist
    void prePersist() {
        if (creadaEn == null) {
            creadaEn = Instant.now();
        }
    }
}
