package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plan_sede")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanSedeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanMembresiaEntity plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private SedeEntity sede;

    @Column(name = "precio_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioActual;

    @Column(name = "recargo_multisede", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal recargoMultisede = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}
