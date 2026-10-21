package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmpleadoRequest {

    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    private String nombre;

    @Size(max = 80, message = "El apellido no puede superar los 80 caracteres")
    private String apellido;

    private Short rolId;

    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    @Email(message = "El correo no es válido")
    @Size(max = 160, message = "El correo no puede superar los 160 caracteres")
    private String correo;

    @Size(max = 250, message = "La dirección no puede superar los 250 caracteres")
    private String direccion;

    @Size(max = 120, message = "La especialidad no puede superar los 120 caracteres")
    private String especialidad;
}
