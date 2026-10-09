package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePlanMembresiaRequest {

    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    private String nombre;

    @Min(value = 1, message = "La duración debe ser de al menos 1 día")
    private Integer duracionDias;

    private Boolean activo;

    @Size(max = 1000, message = "Las condiciones no pueden superar los 1000 caracteres")
    private String condiciones;

    private LocalDate vigenciaDesde;

    private LocalDate vigenciaHasta;
}
