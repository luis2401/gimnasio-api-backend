package com.example.demo.service;

import com.example.demo.Model.PagoModel;
import com.example.demo.Model.SuscripcionModel;
import com.example.demo.Model.VentaMapping;
import com.example.demo.Model.VentaMembresiaModel;
import com.example.demo.dto.CambiarEstadoSuscripcionRequest;
import com.example.demo.dto.CreateSuscripcionRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.PagoRepository;
import com.example.demo.repository.PlanSedeRepository;
import com.example.demo.repository.SocioRepository;
import com.example.demo.repository.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SuscripcionService {

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PlanSedeRepository planSedeRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private VentaMapping mapper;

    @Transactional
    public List<SuscripcionModel> buscarSuscripciones(Long socioId, EstadoSuscripcion estado) {
        suscripcionRepository.marcarVencidas(LocalDate.now());

        List<SuscripcionEntity> lista;
        if (socioId != null) {
            lista = suscripcionRepository.findBySocioIdOrderByFechaInicioDesc(socioId);
            if (estado != null) {
                lista = lista.stream().filter(s -> s.getEstado() == estado).toList();
            }
        } else if (estado != null) {
            lista = suscripcionRepository.findByEstado(estado);
        } else {
            lista = suscripcionRepository.findAll();
        }
        return mapper.convertSuscripciones(lista);
    }

    @Transactional
    public SuscripcionModel buscarPorId(Integer id) {
        suscripcionRepository.marcarVencidas(LocalDate.now());
        return mapper.convert(obtener(id));
    }

    @Transactional
    public List<SuscripcionModel> buscarPorVencer(int dias) {
        if (dias < 1 || dias > 365) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los días deben estar entre 1 y 365");
        }
        LocalDate hoy = LocalDate.now();
        suscripcionRepository.marcarVencidas(hoy);
        return mapper.convertSuscripciones(suscripcionRepository
                .findByEstadoAndFechaFinBetweenOrderByFechaFinAsc(EstadoSuscripcion.ACTIVA, hoy, hoy.plusDays(dias)));
    }

    @Transactional
    public SuscripcionModel obtenerVigenteDeSocio(Long socioId) {
        suscripcionRepository.marcarVencidas(LocalDate.now());
        return suscripcionRepository
                .findFirstBySocioIdAndEstadoAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
                        socioId, EstadoSuscripcion.ACTIVA, LocalDate.now())
                .map(mapper::convert)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El socio no tiene una membresía vigente"));
    }

    /** Vende una membresía: crea la suscripción (con fotografía del plan) y registra su pago. */
    @Transactional
    public VentaMembresiaModel crearSuscripcion(CreateSuscripcionRequest request) {
        EmpleadoEntity vendedor = EmpleadoActual.obtener();

        SocioEntity socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Socio no encontrado con id: " + request.getSocioId()));
        if (!socio.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El socio está inactivo");
        }

        PlanSedeEntity planSede = planSedeRepository.findById(request.getPlanSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Plan de sede no encontrado con id: " + request.getPlanSedeId()));
        PlanMembresiaEntity plan = planSede.getPlan();

        if (!planSede.isActivo() || !plan.isActivo() || !planSede.getSede().isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plan o la sede no están disponibles");
        }

        LocalDate hoy = LocalDate.now();
        if ((plan.getVigenciaDesde() != null && hoy.isBefore(plan.getVigenciaDesde()))
                || (plan.getVigenciaHasta() != null && hoy.isAfter(plan.getVigenciaHasta()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plan no está vigente para su venta");
        }

        LocalDate inicio = request.getFechaInicio() != null ? request.getFechaInicio() : hoy;
        suscripcionRepository.marcarVencidas(hoy);
        if (suscripcionRepository.existsBySocioIdAndEstadoAndFechaFinGreaterThanEqual(
                socio.getId(), EstadoSuscripcion.ACTIVA, inicio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Regla de negocio: el socio ya tiene una membresía activa en esa fecha");
        }

        boolean multisede = Boolean.TRUE.equals(request.getAccesoMultisede());
        BigDecimal recargo = multisede ? planSede.getRecargoMultisede() : BigDecimal.ZERO;
        BigDecimal precioLista = planSede.getPrecioActual().add(recargo);
        BigDecimal precioVendido = request.getPrecioVendido() != null ? request.getPrecioVendido() : precioLista;

        if (precioVendido.compareTo(precioLista) != 0
                && (request.getObservacionPrecio() == null || request.getObservacionPrecio().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar una observación cuando el precio vendido difiere del precio de lista");
        }
        if (precioVendido.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio vendido debe ser mayor a 0");
        }

        validarNumeroOperacion(request.getNumeroOperacion());

        SuscripcionEntity suscripcion = suscripcionRepository.save(SuscripcionEntity.builder()
                .socio(socio)
                .planSede(planSede)
                .nombrePlanVendido(plan.getNombre())
                .duracionDiasVendida(plan.getDuracionDias())
                .precioVendido(precioVendido)
                .precioLista(precioLista)
                .condicionesVendidas(plan.getCondiciones())
                .fechaInicio(inicio)
                .fechaFin(inicio.plusDays(plan.getDuracionDias()))
                .estado(EstadoSuscripcion.ACTIVA)
                .observacionPrecio(request.getObservacionPrecio())
                .accesoMultisede(multisede)
                .recargoMultisede(recargo)
                .build());

        PagoEntity pago = pagoRepository.save(PagoEntity.builder()
                .monto(precioVendido)
                .metodo(request.getMetodoPago())
                .suscripcion(suscripcion)
                .sede(planSede.getSede())
                .vendedor(vendedor)
                .numeroOperacion(blankToNull(request.getNumeroOperacion()))
                .estado(EstadoPago.PAGADO)
                .tipoVenta(TipoVenta.MEMBRESIA)
                .observacionPrecio(request.getObservacionPrecio())
                .build());

        return VentaMembresiaModel.builder()
                .suscripcion(mapper.convert(suscripcion))
                .pago(mapper.convert(pago))
                .build();
    }

    @Transactional
    public SuscripcionModel cambiarEstado(Integer id, CambiarEstadoSuscripcionRequest request) {
        SuscripcionEntity suscripcion = obtener(id);
        EstadoSuscripcion actual = suscripcion.getEstado();
        EstadoSuscripcion nuevo = request.getEstado();

        if (actual == EstadoSuscripcion.CANCELADA || actual == EstadoSuscripcion.VENCIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede modificar una suscripción " + actual);
        }
        if (nuevo != EstadoSuscripcion.ACTIVA && nuevo != EstadoSuscripcion.SUSPENDIDA
                && nuevo != EstadoSuscripcion.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se permite cambiar a ACTIVA, SUSPENDIDA o CANCELADA");
        }
        if (nuevo == EstadoSuscripcion.ACTIVA && suscripcion.getFechaFin().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede reactivar una suscripción cuya fecha de fin ya pasó");
        }

        suscripcion.setEstado(nuevo);
        return mapper.convert(suscripcionRepository.saveAndFlush(suscripcion));
    }

    @Transactional(readOnly = true)
    public List<PagoModel> obtenerPagos(Integer id) {
        obtener(id);
        return mapper.convertPagos(pagoRepository.findBySuscripcionId(id));
    }

    private SuscripcionEntity obtener(Integer id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Suscripción no encontrada con id: " + id));
    }

    private void validarNumeroOperacion(String numeroOperacion) {
        if (numeroOperacion != null && !numeroOperacion.isBlank()
                && pagoRepository.existsByNumeroOperacion(numeroOperacion.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Regla de negocio: ya existe un pago con el número de operación " + numeroOperacion);
        }
    }

    private String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
