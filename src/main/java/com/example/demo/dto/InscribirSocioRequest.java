package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InscribirSocioRequest {

    @NotNull(message = "El socio es obligatorio")
    private Long socioId;

    @NotNull(message = "La clase es obligatoria")
    private Integer claseId;
}
