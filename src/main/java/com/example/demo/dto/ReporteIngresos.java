package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReporteIngresos {

    private BigDecimal total;
    private long cantidadPagos;
    private Map<String, BigDecimal> porMetodo;
    private Map<String, BigDecimal> porTipoVenta;
}
