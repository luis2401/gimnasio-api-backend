package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSedeRequest {

    @NotBlank(message = "El código de la sede es obligatorio")
    @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre de la sede es obligatorio")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    private String nombre;

    @NotBlank(message = "La dirección de la sede es obligatoria")
    @Size(min = 5, max = 250, message = "La dirección debe tener entre 5 y 250 caracteres")
    private String direccion;

    @NotNull(message = "El campo activa es obligatorio")
    private Boolean activa;
}
