package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AsignarPrecioSedeRequest {

    @NotNull(message = "El ID de la sede es obligatorio")
    private Integer sedeId;

    @NotNull(message = "El precio actual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precioActual;

    @DecimalMin(value = "0.0", inclusive = true, message = "El recargo multisede debe ser mayor o igual a 0")
    @Builder.Default
    private BigDecimal recargoMultisede = BigDecimal.ZERO;

    @Builder.Default
    private Boolean activo = true;
}
