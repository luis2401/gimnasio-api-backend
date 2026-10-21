package com.example.demo.Model;

import com.example.demo.entity.EstadoAsistencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AsistenciaClaseModel {

    private Long id;
    private Long socioClaseId;
    private Long socioId;
    private String socioNombre;
    private Integer claseId;
    private String claseNombre;
    private LocalDate fecha;
    private EstadoAsistencia estado;
    private Integer registradoPorId;
    private Instant registradoEn;
    private String observacion;
}
