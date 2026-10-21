package com.example.demo.dto;

import com.example.demo.entity.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CreatePaseDiarioRequest {

    @NotNull(message = "La sede es obligatoria")
    private Integer sedeId;

    @NotBlank(message = "El nombre del visitante es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String visitanteNombre;

    @Size(max = 20, message = "El documento no puede superar los 20 caracteres")
    private String visitanteDocumento;

    /** Si es null se usa la fecha de hoy. */
    private LocalDate fechaAcceso;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @Size(max = 48, message = "El número de operación no puede superar los 48 caracteres")
    private String numeroOperacion;
}
