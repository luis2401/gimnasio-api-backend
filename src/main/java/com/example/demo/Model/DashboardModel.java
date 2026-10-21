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
public class DashboardModel {

    private long sociosActivos;
    private long sociosInactivos;
    private long suscripcionesActivas;
    private long suscripcionesPorVencer7Dias;
    private BigDecimal ingresosHoy;
    private BigDecimal ingresosMes;
    private long clasesActivas;
    private long empleadosActivos;
    private long sedesActivas;
}
