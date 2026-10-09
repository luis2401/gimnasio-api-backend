package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanSedeModel {

    private Integer id;
    private Integer planId;
    private String planNombre;
    private Integer sedeId;
    private String sedeCodigo;
    private String sedeNombre;
    private BigDecimal precioActual;
    private BigDecimal recargoMultisede;
    private boolean activo;
}
