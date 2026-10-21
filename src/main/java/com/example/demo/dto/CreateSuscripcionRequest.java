package com.example.demo.dto;

import com.example.demo.entity.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSuscripcionRequest {

    @NotNull(message = "El socio es obligatorio")
    private Long socioId;

    @NotNull(message = "El plan de la sede es obligatorio")
    private Integer planSedeId;

    /** Si es null se usa la fecha de hoy. */
    private LocalDate fechaInicio;

    /** Si es true se suma el recargo multisede al precio de lista. */
    private Boolean accesoMultisede;

    /** Si es null se cobra el precio de lista. Si difiere, se exige observación. */
    @DecimalMin(value = "0.01", message = "El precio vendido debe ser mayor a 0")
    private BigDecimal precioVendido;

    @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
    private String observacionPrecio;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @Size(max = 48, message = "El número de operación no puede superar los 48 caracteres")
    private String numeroOperacion;
}
