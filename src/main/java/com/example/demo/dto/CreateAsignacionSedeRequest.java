package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAsignacionSedeRequest {

    @NotNull(message = "El empleado es obligatorio")
    private Integer empleadoId;

    @NotNull(message = "La sede es obligatoria")
    private Integer sedeId;

    /** Si es null se usa la fecha de hoy. */
    private LocalDate fechaDesde;

    private LocalDate fechaHasta;
}
