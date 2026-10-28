package com.example.demo.service;

import com.example.demo.Model.PagoModel;
import com.example.demo.Model.VentaMapping;
import com.example.demo.dto.AnularPagoRequest;
import com.example.demo.dto.CreatePaseDiarioRequest;
import com.example.demo.dto.ReporteIngresos;
import com.example.demo.entity.*;
import com.example.demo.repository.PagoRepository;
import com.example.demo.repository.SedeRepository;
import com.example.demo.repository.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private VentaMapping mapper;

    private boolean esRolGlobal(EmpleadoEntity empleado) {
        if (empleado == null || empleado.getRol() == null) return false;
        String cod = empleado.getRol().getCodigo();
        if (cod == null) cod = empleado.getRol().getNombre();
        if (cod == null) return false;
        String upper = cod.toUpperCase().trim();
        return "ADMINISTRADOR".equals(upper) || "ADMIN".equals(upper) || "SUPERVISOR".equals(upper);
    }

    private EmpleadoEntity obtenerEmpleadoActualOpcional() {
        try {
            return EmpleadoActual.obtener();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<PagoModel> buscarPagos(Integer sedeId) {
        EmpleadoEntity emp = obtenerEmpleadoActualOpcional();
        boolean global = emp == null || esRolGlobal(emp);

        List<PagoEntity> lista;
        if (global) {
            lista = sedeId != null
                    ? pagoRepository.findBySedeIdOrderByFechaPagoDesc(sedeId)
                    : pagoRepository.findAll(org.springframework.data.domain.Sort.by("fechaPago").descending());
        } else {
            // Vendedor estándar: solo ve sus propias ventas (incluso si vendió en otra sede por rotación)
            lista = sedeId != null
                    ? pagoRepository.findByVendedorIdAndSedeIdOrderByFechaPagoDesc(emp.getId(), sedeId)
                    : pagoRepository.findByVendedorIdOrderByFechaPagoDesc(emp.getId());
        }
        return mapper.convertPagos(lista);
    }

    @Transactional(readOnly = true)
    public com.example.demo.Model.PaginaModel<PagoModel> buscarPagina(Integer sedeId, int page, int size) {
        org.springframework.data.domain.Pageable pageable = Paginacion.de(page, size,
                org.springframework.data.domain.Sort.by("fechaPago").descending());
        EmpleadoEntity emp = obtenerEmpleadoActualOpcional();
        boolean global = emp == null || esRolGlobal(emp);

        org.springframework.data.domain.Page<PagoEntity> pagina;
        if (global) {
            pagina = sedeId != null
                    ? pagoRepository.findBySedeId(sedeId, pageable)
                    : pagoRepository.findAll(pageable);
        } else {
            pagina = sedeId != null
                    ? pagoRepository.findByVendedorIdAndSedeId(emp.getId(), sedeId, pageable)
                    : pagoRepository.findByVendedorId(emp.getId(), pageable);
        }
        return com.example.demo.Model.PaginaModel.from(pagina.map(e -> mapper.convert(e)));
    }

    @Transactional(readOnly = true)
    public PagoModel buscarPorId(Integer id) {
        PagoEntity pago = obtener(id);
        EmpleadoEntity emp = obtenerEmpleadoActualOpcional();
        if (emp != null && !esRolGlobal(emp)) {
            if (pago.getVendedor() == null || !pago.getVendedor().getId().equals(emp.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver comprobantes de otros vendedores");
            }
        }
        return mapper.convert(pago);
    }

    @Transactional
    public PagoModel registrarPaseDiario(CreatePaseDiarioRequest request) {
        EmpleadoEntity vendedor = EmpleadoActual.obtener();

        SedeEntity sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sede no encontrada con id: " + request.getSedeId()));
        if (!sede.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sede está inactiva");
        }

        String numeroOperacion = request.getNumeroOperacion() == null || request.getNumeroOperacion().isBlank()
                ? null : request.getNumeroOperacion().trim();
        if (numeroOperacion == null) {
            numeroOperacion = SuscripcionService.generarNopAutomatico(sede, vendedor, request.getMetodoPago());
        }
        if (pagoRepository.existsByNumeroOperacion(numeroOperacion)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Regla de negocio: ya existe un pago con el número de operación " + numeroOperacion);
        }

        PagoEntity pago = pagoRepository.save(PagoEntity.builder()
                .monto(request.getMonto())
                .metodo(request.getMetodoPago())
                .sede(sede)
                .vendedor(vendedor)
                .numeroOperacion(numeroOperacion)
                .estado(EstadoPago.PAGADO)
                .tipoVenta(TipoVenta.PASE_DIARIO)
                .visitanteNombre(request.getVisitanteNombre().trim())
                .visitanteDocumento(request.getVisitanteDocumento())
                .fechaAcceso(request.getFechaAcceso() != null ? request.getFechaAcceso() : LocalDate.now())
                .build());

        return mapper.convert(pago);
    }

    /** Anula un pago; si era de una membresía, cancela también la suscripción. */
    @Transactional
    public PagoModel anularPago(Integer id, AnularPagoRequest request) {
        PagoEntity pago = obtener(id);
        if (pago.getEstado() == EstadoPago.ANULADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pago ya está anulado");
        }

        pago.setEstado(EstadoPago.ANULADO);
        pago.setMotivoAnulacion(request.getMotivo().trim());
        pago.setAnuladoPor(EmpleadoActual.obtener());
        pago.setFechaAnulacion(Instant.now());

        SuscripcionEntity suscripcion = pago.getSuscripcion();
        if (suscripcion != null && suscripcion.getEstado() != EstadoSuscripcion.CANCELADA) {
            suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
            suscripcionRepository.save(suscripcion);
        }

        return mapper.convert(pagoRepository.saveAndFlush(pago));
    }

    @Transactional(readOnly = true)
    public ReporteIngresos generarReporte(LocalDate desde, LocalDate hasta, Integer sedeId) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        if (fin.isBefore(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha 'hasta' no puede ser anterior a 'desde'");
        }

        ZoneId zona = ZoneId.systemDefault();
        Instant desdeInstant = inicio.atStartOfDay(zona).toInstant();
        Instant hastaInstant = fin.plusDays(1).atStartOfDay(zona).toInstant();

        List<PagoEntity> pagos = sedeId != null
                ? pagoRepository.findBySedeIdAndFechaPagoBetweenAndEstado(sedeId, desdeInstant, hastaInstant, EstadoPago.PAGADO)
                : pagoRepository.findByFechaPagoBetweenAndEstado(desdeInstant, hastaInstant, EstadoPago.PAGADO);

        Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
        Map<String, BigDecimal> porTipo = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PagoEntity p : pagos) {
            total = total.add(p.getMonto());
            porMetodo.merge(p.getMetodo().name(), p.getMonto(), BigDecimal::add);
            porTipo.merge(p.getTipoVenta().name(), p.getMonto(), BigDecimal::add);
        }

        return ReporteIngresos.builder()
                .total(total)
                .cantidadPagos(pagos.size())
                .porMetodo(porMetodo)
                .porTipoVenta(porTipo)
                .build();
    }

    private PagoEntity obtener(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pago no encontrado con id: " + id));
    }
}
