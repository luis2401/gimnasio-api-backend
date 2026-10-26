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
public class SocioClaseModel {

    private Long id;
    private Long socioId;
    private String socioDni;
    private String socioNombre;
    private Integer claseId;
    private String claseNombre;
    private Integer sedeId;
    private String sedeNombre;
    private LocalDate fechaInscripcion;
    private LocalDate fechaBaja;
    private boolean activo;
}
