package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioModel {

    private Long id;
    private String username;
    private Integer empleadoId;
    private String empleadoNombre;
    private String rolCodigo;
    private boolean activo;
    private Integer intentosFallidos;
    private Integer nivelBloqueo;
    private Date bloqueadoHasta;
    private boolean bloqueado;
    private Date ultimoAcceso;
    private Date fechaCreacion;
}
