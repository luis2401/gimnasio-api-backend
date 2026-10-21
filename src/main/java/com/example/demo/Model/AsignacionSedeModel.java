package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AsignacionSedeModel {

    private Integer id;
    private Integer empleadoId;
    private String empleadoNombre;
    private Integer sedeId;
    private String sedeNombre;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private boolean anulada;
}
