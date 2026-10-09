package com.example.demo.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateSedeRequest {

    @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
    private String codigo;

    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    private String nombre;

    @Size(min = 5, max = 250, message = "La dirección debe tener entre 5 y 250 caracteres")
    private String direccion;

    private Boolean activa;
}
