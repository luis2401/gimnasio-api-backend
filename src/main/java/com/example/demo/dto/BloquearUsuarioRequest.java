package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BloquearUsuarioRequest {

    @NotNull(message = "La duración del bloqueo en minutos es obligatoria")
    @Min(value = 1, message = "El bloqueo debe durar al menos 1 minuto")
    @Max(value = 525600, message = "El bloqueo no puede superar 1 año")
    private Integer minutos;
}
