package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SedeModel {

    private Integer id;
    private String codigo;
    private String nombre;
    private String direccion;
    private boolean activa;
}
