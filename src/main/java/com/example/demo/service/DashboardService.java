package com.example.demo.service;

import com.example.demo.Model.DashboardModel;
import com.example.demo.entity.EstadoSuscripcion;
import com.example.demo.repository.ClaseRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.SedeRepository;
import com.example.demo.repository.SocioRepository;
import com.example.demo.repository.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DashboardService {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private PagoService pagoService;

    @Transactional
    public DashboardModel generarDashboard() {
        LocalDate hoy = LocalDate.now();
        suscripcionRepository.marcarVencidas(hoy);

        return DashboardModel.builder()
                .sociosActivos(socioRepository.countByActivo(true))
                .sociosInactivos(socioRepository.countByActivo(false))
                .suscripcionesActivas(suscripcionRepository.countByEstado(EstadoSuscripcion.ACTIVA))
                .suscripcionesPorVencer7Dias(suscripcionRepository
                        .findByEstadoAndFechaFinBetweenOrderByFechaFinAsc(EstadoSuscripcion.ACTIVA, hoy, hoy.plusDays(7))
                        .size())
                .ingresosHoy(pagoService.generarReporte(hoy, hoy, null).getTotal())
                .ingresosMes(pagoService.generarReporte(hoy.withDayOfMonth(1), hoy, null).getTotal())
                .clasesActivas(claseRepository.countByActivo(true))
                .empleadosActivos(empleadoRepository.countByActivo(true))
                .sedesActivas(sedeRepository.countByActiva(true))
                .build();
    }
}
