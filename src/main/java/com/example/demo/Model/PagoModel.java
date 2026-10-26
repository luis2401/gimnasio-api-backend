package com.example.demo.Model;

import com.example.demo.entity.EstadoPago;
import com.example.demo.entity.MetodoPago;
import com.example.demo.entity.TipoVenta;
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
public class PagoModel {

    private Integer id;
    private Instant fechaPago;
    private BigDecimal monto;
    private MetodoPago metodo;
    private Integer suscripcionId;
    private String socioNombre;
    private String nombrePlan;
    private Integer sedeId;
    private String sedeNombre;
    private Integer vendedorId;
    private String vendedorNombre;
    private String numeroOperacion;
    private EstadoPago estado;
    private String motivoAnulacion;
    private Integer anuladoPorId;
    private Instant fechaAnulacion;
    private TipoVenta tipoVenta;
    private String visitanteNombre;
    private String visitanteDocumento;
    private LocalDate fechaAcceso;
    private String observacionPrecio;
}
