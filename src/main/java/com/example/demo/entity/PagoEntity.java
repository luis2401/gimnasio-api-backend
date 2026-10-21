package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_pago", nullable = false)
    private Instant fechaPago;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPago metodo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "suscripcion_id")
    private SuscripcionEntity suscripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private SedeEntity sede;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private EmpleadoEntity vendedor;

    @Column(name = "numero_operacion", length = 48)
    private String numeroOperacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoPago estado = EstadoPago.PAGADO;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "anulado_por")
    private EmpleadoEntity anuladoPor;

    @Column(name = "fecha_anulacion")
    private Instant fechaAnulacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta", nullable = false, length = 20)
    @Builder.Default
    private TipoVenta tipoVenta = TipoVenta.MEMBRESIA;

    @Column(name = "visitante_nombre", length = 120)
    private String visitanteNombre;

    @Column(name = "visitante_documento", length = 20)
    private String visitanteDocumento;

    @Column(name = "fecha_acceso")
    private LocalDate fechaAcceso;

    @Column(name = "observacion_precio", length = 500)
    private String observacionPrecio;

    @PrePersist
    void prePersist() {
        if (fechaPago == null) {
            fechaPago = Instant.now();
        }
    }
}
