package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateClaseRequest {

    @NotBlank(message = "El nombre de la clase es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotNull(message = "El día de la semana es obligatorio")
    @Min(value = 1, message = "El día debe estar entre 1 (lunes) y 7 (domingo)")
    @Max(value = 7, message = "El día debe estar entre 1 (lunes) y 7 (domingo)")
    private Integer diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 1, message = "El cupo máximo debe ser al menos 1")
    private Integer cupoMax;

    @NotNull(message = "El instructor es obligatorio")
    private Integer instructorId;

    @NotNull(message = "La sede es obligatoria")
    private Integer sedeId;
}
