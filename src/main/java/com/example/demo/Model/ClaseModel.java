package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaseModel {

    private Integer id;
    private String nombre;
    private Integer diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer cupoMax;
    private long inscritos;
    private boolean activo;
    private Integer instructorId;
    private String instructorNombre;
    private Integer sedeId;
    private String sedeNombre;
}
