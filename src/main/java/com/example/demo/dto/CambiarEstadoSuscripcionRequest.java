package com.example.demo.dto;

import com.example.demo.entity.EstadoSuscripcion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CambiarEstadoSuscripcionRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoSuscripcion estado;
}
