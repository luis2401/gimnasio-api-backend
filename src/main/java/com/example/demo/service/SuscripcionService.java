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

    /** Administradores y supervisores ven todas las ventas; el resto solo las que vendió. */
    private boolean esRolGlobal(EmpleadoEntity empleado) {
        if (empleado == null || empleado.getRol() == null) return false;
        String cod = empleado.getRol().getCodigo();
        if (cod == null) cod = empleado.getRol().getNombre();
        if (cod == null) return false;
        String upper = cod.toUpperCase().trim();
        return "ADMINISTRADOR".equals(upper) || "ADMIN".equals(upper) || "SUPERVISOR".equals(upper);
    }

    private EmpleadoEntity empleadoActualOpcional() {
        try {
            return EmpleadoActual.obtener();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Ids de las suscripciones vendidas por el empleado en sesión, o null si puede ver todas.
     * La autoría de la venta se toma del pago de membresía asociado (pago.vendedor).
     */
    private java.util.Set<Integer> idsVendidasPorActual() {
        EmpleadoEntity emp = empleadoActualOpcional();
        if (emp == null || esRolGlobal(emp)) return null;
        return pagoRepository.findByVendedorIdOrderByFechaPagoDesc(emp.getId()).stream()
                .filter(p -> p.getSuscripcion() != null)
                .map(p -> p.getSuscripcion().getId())
                .collect(java.util.stream.Collectors.toSet());
    }

    private List<SuscripcionEntity> filtrarPorVendedor(List<SuscripcionEntity> lista) {
        java.util.Set<Integer> ids = idsVendidasPorActual();
        if (ids == null) return lista;
        return lista.stream().filter(s -> ids.contains(s.getId())).toList();
    }

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
        // Las consultas por socio (usadas para calcular el encadenamiento al renovar) no se filtran.
        if (socioId == null) {
            lista = filtrarPorVendedor(lista);
        }
        return mapper.convertSuscripciones(lista);
    }

    @Transactional
    public com.example.demo.Model.PaginaModel<SuscripcionModel> buscarPagina(EstadoSuscripcion estado, int page, int size) {
        suscripcionRepository.marcarVencidas(LocalDate.now());
        org.springframework.data.domain.Pageable pageable = Paginacion.de(page, size,
                org.springframework.data.domain.Sort.by("id").descending());
        java.util.Set<Integer> ids = idsVendidasPorActual();
        if (ids != null) {
            // Vendedor estándar: paginamos en memoria solo sobre sus propias ventas
            List<SuscripcionEntity> propias = filtrarPorVendedor(estado != null
                    ? suscripcionRepository.findByEstado(estado)
                    : suscripcionRepository.findAll(org.springframework.data.domain.Sort.by("id").descending()));
            int desde = (int) Math.min(pageable.getOffset(), propias.size());
            int hasta = Math.min(desde + pageable.getPageSize(), propias.size());
            org.springframework.data.domain.Page<SuscripcionEntity> propiasPagina =
                    new org.springframework.data.domain.PageImpl<>(propias.subList(desde, hasta), pageable, propias.size());
            return com.example.demo.Model.PaginaModel.from(propiasPagina.map(e -> mapper.convert(e)));
        }
        org.springframework.data.domain.Page<SuscripcionEntity> pagina = estado != null
                ? suscripcionRepository.findByEstado(estado, pageable)
                : suscripcionRepository.findAll(pageable);
        return com.example.demo.Model.PaginaModel.from(pagina.map(e -> mapper.convert(e)));
    }

    @Transactional
    public SuscripcionModel buscarPorId(Integer id) {
        suscripcionRepository.marcarVencidas(LocalDate.now());
        java.util.Set<Integer> ids = idsVendidasPorActual();
        if (ids != null && !ids.contains(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puedes consultar las membresías que tú vendiste");
        }
        return mapper.convert(obtener(id));
    }

    @Transactional
    public List<SuscripcionModel> buscarPorVencer(int dias) {
        if (dias < 1 || dias > 365) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los días deben estar entre 1 y 365");
        }
        LocalDate hoy = LocalDate.now();
        suscripcionRepository.marcarVencidas(hoy);

        List<SuscripcionEntity> candidatos = suscripcionRepository
                .findByEstadoAndFechaFinBetweenOrderByFechaFinAsc(EstadoSuscripcion.ACTIVA, hoy, hoy.plusDays(dias));

        // Filtrar suscripciones: si el socio ya tiene OTRA suscripción activa con fechaFin posterior a esta,
        // no se considera por vencer ya que cuenta con extensión continua / acumulada garantizada.
        List<SuscripcionEntity> realmentePorVencer = candidatos.stream()
                .filter(s -> !suscripcionRepository.existsBySocioIdAndEstadoAndFechaFinGreaterThan(
                        s.getSocio().getId(), EstadoSuscripcion.ACTIVA, s.getFechaFin()))
                .toList();

        return mapper.convertSuscripciones(filtrarPorVendedor(realmentePorVencer));
    }

    @Transactional
    public SuscripcionModel obtenerVigenteDeSocio(Long socioId) {
        suscripcionRepository.marcarVencidas(LocalDate.now());
        return suscripcionRepository
                .findFirstBySocioIdAndEstadoAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
                        socioId, EstadoSuscripcion.ACTIVA, LocalDate.now())
                .map(mapper::convert)
                .orElse(null);
    }

    /** Vende una membresía: crea la suscripción (con fotografía del plan) y registra su pago. */
    @Transactional
    public VentaMembresiaModel crearSuscripcion(CreateSuscripcionRequest request) {
        EmpleadoEntity vendedor = EmpleadoActual.obtener();

        SocioEntity socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Socio no encontrado con id: " + request.getSocioId()));
        if (!socio.isActivo()) {
            socio.setActivo(true);
            socio = socioRepository.save(socio);
        }

        PlanSedeEntity planSede = planSedeRepository.findById(request.getPlanSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Plan de sede no encontrado con id: " + request.getPlanSedeId()));
        PlanMembresiaEntity plan = planSede.getPlan();

        if (!planSede.isActivo() || !plan.isActivo() || !planSede.getSede().isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plan o la sede seleccionada no están activos");
        }

        LocalDate hoy = LocalDate.now();
        if ((plan.getVigenciaDesde() != null && hoy.isBefore(plan.getVigenciaDesde()))
                || (plan.getVigenciaHasta() != null && hoy.isAfter(plan.getVigenciaHasta()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plan no está vigente para su venta en la fecha actual");
        }

        LocalDate inicio = request.getFechaInicio() != null ? request.getFechaInicio() : hoy;
        suscripcionRepository.marcarVencidas(hoy);

        // Si el socio ya tiene una membresía activa vigente, tomamos el último día de su suscripción actual
        // y a partir de esa fecha sumamos los días del nuevo plan contratado.
        SuscripcionEntity vigenteActual = suscripcionRepository
                .findFirstBySocioIdAndEstadoAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
                        socio.getId(), EstadoSuscripcion.ACTIVA, hoy)
                .orElse(null);

        boolean esExtensionContinua = false;
        if (vigenteActual != null) {
            LocalDate ultimoDiaSuscripcion = vigenteActual.getFechaFin();
            if (!inicio.isAfter(ultimoDiaSuscripcion)) {
                inicio = ultimoDiaSuscripcion;
                esExtensionContinua = true;
            }
        }

        boolean multisede = Boolean.TRUE.equals(request.getAccesoMultisede());
        BigDecimal recargo = multisede ? planSede.getRecargoMultisede() : BigDecimal.ZERO;
        BigDecimal precioLista = planSede.getPrecioActual().add(recargo);
        BigDecimal precioVendido = request.getPrecioVendido() != null ? request.getPrecioVendido() : precioLista;

        if (precioVendido.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio vendido debe ser mayor a 0");
        }

        String observacionEfectiva = blankToNull(request.getObservacionPrecio());
        if (precioVendido.compareTo(precioLista) != 0 && observacionEfectiva == null) {
            observacionEfectiva = "Tarifa ajustada en caja (Precio lista: S/ " + precioLista + ")";
        } else if (esExtensionContinua && observacionEfectiva == null) {
            observacionEfectiva = "Renovación acumulada desde el último día de su membresía vigente (" + inicio + ")";
        }

        String nopEfectivo = blankToNull(request.getNumeroOperacion());
        if (nopEfectivo == null) {
            nopEfectivo = generarNopAutomatico(planSede.getSede(), vendedor, request.getMetodoPago());
        }
        validarNumeroOperacion(nopEfectivo);

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
                .observacionPrecio(observacionEfectiva)
                .accesoMultisede(multisede)
                .recargoMultisede(recargo)
                .build());

        PagoEntity pago = pagoRepository.save(PagoEntity.builder()
                .monto(precioVendido)
                .metodo(request.getMetodoPago())
                .suscripcion(suscripcion)
                .sede(planSede.getSede())
                .vendedor(vendedor)
                .numeroOperacion(nopEfectivo)
                .estado(EstadoPago.PAGADO)
                .tipoVenta(TipoVenta.MEMBRESIA)
                .observacionPrecio(observacionEfectiva)
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

    public static String generarNopAutomatico(com.example.demo.entity.SedeEntity sede, com.example.demo.entity.EmpleadoEntity vendedor, com.example.demo.entity.MetodoPago metodo) {
        String sedeCode = "SC";
        if (sede != null && sede.getNombre() != null) {
            String[] words = sede.getNombre().trim().split("\\s+");
            if (words.length >= 2) {
                sedeCode = ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
            } else if (words.length == 1 && words[0].length() >= 2) {
                sedeCode = words[0].substring(0, 2).toUpperCase();
            }
        }

        String vendCode = "AD";
        if (vendedor != null) {
            String nom = vendedor.getNombre() != null && !vendedor.getNombre().isBlank() ? "" + vendedor.getNombre().trim().charAt(0) : "A";
            String ape = vendedor.getApellido() != null && !vendedor.getApellido().isBlank() ? "" + vendedor.getApellido().trim().charAt(0) : "D";
            vendCode = (nom + ape).toUpperCase();
        }

        String metCode = "E";
        if (metodo != null) {
            switch (metodo) {
                case YAPE -> metCode = "Y";
                case PLIN -> metCode = "P";
                case TARJETA -> metCode = "T";
                case TRANSFERENCIA -> metCode = "TR";
                case EFECTIVO -> metCode = "E";
            }
        }

        int randomNum = 100000 + (int)(Math.random() * 900000);
        return sedeCode + "-" + vendCode + metCode + "-" + randomNum;
    }

    private String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
