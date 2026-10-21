package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateClaseRequest {

    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @Min(value = 1, message = "El día debe estar entre 1 (lunes) y 7 (domingo)")
    @Max(value = 7, message = "El día debe estar entre 1 (lunes) y 7 (domingo)")
    private Integer diaSemana;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    @Min(value = 1, message = "El cupo máximo debe ser al menos 1")
    private Integer cupoMax;

    private Integer instructorId;
}
