package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanMembresiaModel {

    private Integer id;
    private String nombre;
    private Integer duracionDias;
    private boolean activo;
    private String condiciones;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private List<PlanSedeModel> preciosSedes;
}
