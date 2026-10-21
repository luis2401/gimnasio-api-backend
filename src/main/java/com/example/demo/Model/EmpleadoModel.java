package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpleadoModel {

    private Integer id;
    private String dni;
    private String nombre;
    private String apellido;
    private Short rolId;
    private String rolCodigo;
    private boolean activo;
    private String telefono;
    private String correo;
    private String direccion;
    private String especialidad;
}
