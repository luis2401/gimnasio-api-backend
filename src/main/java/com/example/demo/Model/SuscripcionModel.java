package com.example.demo.Model;

import com.example.demo.entity.EstadoSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuscripcionModel {

    private Integer id;
    private Long socioId;
    private String socioDni;
    private String socioNombre;
    private String socioTelefono;
    private Integer planSedeId;
    private Integer sedeId;
    private String sedeNombre;
    private String nombrePlanVendido;
    private Integer duracionDiasVendida;
    private BigDecimal precioVendido;
    private BigDecimal precioLista;
    private String condicionesVendidas;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSuscripcion estado;
    private Instant creadaEn;
    private String observacionPrecio;
    private boolean accesoMultisede;
    private BigDecimal recargoMultisede;
}
