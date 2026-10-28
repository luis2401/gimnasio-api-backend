package com.example.demo.service;

import com.example.demo.Model.PlanMembresiaMapping;
import com.example.demo.Model.PlanMembresiaModel;
import com.example.demo.Model.PlanSedeModel;
import com.example.demo.dto.AsignarPrecioSedeRequest;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreatePlanMembresiaRequest;
import com.example.demo.dto.UpdatePlanMembresiaRequest;
import com.example.demo.entity.PlanMembresiaEntity;
import com.example.demo.entity.PlanSedeEntity;
import com.example.demo.entity.SedeEntity;
import com.example.demo.repository.PlanMembresiaRepository;
import com.example.demo.repository.PlanSedeRepository;
import com.example.demo.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanMembresiaService {

    @Autowired
    private PlanMembresiaRepository planRepository;

    @Autowired
    private PlanSedeRepository planSedeRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private PlanMembresiaMapping mapper;

    @Transactional(readOnly = true)
    public List<PlanMembresiaModel> buscarPlanes(Boolean soloActivos) {
        List<PlanMembresiaEntity> entidades = Boolean.TRUE.equals(soloActivos)
                ? planRepository.findByActivo(true)
                : planRepository.findAll();
        return mapper.convert(entidades);
    }

    @Transactional(readOnly = true)
    public PlanMembresiaModel buscarPlanPorId(Integer id) {
        return mapper.convert(obtenerPlan(id));
    }

    @Transactional
    public PlanMembresiaModel crearPlan(CreatePlanMembresiaRequest request) {
        validarUnicidadNombre(request.getNombre(), null);
        validarRangoVigencia(request.getVigenciaDesde(), request.getVigenciaHasta());

        PlanMembresiaEntity plan = mapper.convert(request);
        if (request.getActivo() != null) {
            plan.setActivo(request.getActivo());
        }
        plan.setPreciosSedes(new ArrayList<>());
        plan = planRepository.saveAndFlush(plan);

        if (request.getPreciosSedes() != null && !request.getPreciosSedes().isEmpty()) {
            for (AsignarPrecioSedeRequest precioReq : request.getPreciosSedes()) {
                asignarOActualizarPrecioSede(plan.getId(), precioReq);
            }
            plan = obtenerPlan(plan.getId());
        } else {
            BigDecimal precioDefecto = plan.getDuracionDias() <= 35
                    ? new BigDecimal("120.00")
                    : plan.getDuracionDias() <= 100
                    ? new BigDecimal("320.00")
                    : new BigDecimal("1000.00");
            for (SedeEntity sedeActiva : sedeRepository.findByActiva(true)) {
                planSedeRepository.saveAndFlush(PlanSedeEntity.builder()
                        .plan(plan)
                        .sede(sedeActiva)
                        .precioActual(precioDefecto)
                        .recargoMultisede(new BigDecimal("20.00"))
                        .activo(true)
                        .build());
            }
            plan = obtenerPlan(plan.getId());
        }

        return mapper.convert(plan);
    }

    @Transactional
    public PlanMembresiaModel actualizarPlan(Integer id, UpdatePlanMembresiaRequest request) {
        PlanMembresiaEntity plan = obtenerPlan(id);

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            validarUnicidadNombre(request.getNombre(), id);
            plan.setNombre(request.getNombre().trim());
        }

        if (request.getDuracionDias() != null) {
            if (request.getDuracionDias() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La duración en días debe ser mayor a 0");
            }
            plan.setDuracionDias(request.getDuracionDias());
        }

        if (request.getCondiciones() != null) {
            plan.setCondiciones(request.getCondiciones());
        }

        if (request.getActivo() != null) {
            plan.setActivo(request.getActivo());
        }

        LocalDate nuevaDesde = request.getVigenciaDesde() != null ? request.getVigenciaDesde() : plan.getVigenciaDesde();
        LocalDate nuevaHasta = request.getVigenciaHasta() != null ? request.getVigenciaHasta() : plan.getVigenciaHasta();
        validarRangoVigencia(nuevaDesde, nuevaHasta);

        if (request.getVigenciaDesde() != null) {
            plan.setVigenciaDesde(request.getVigenciaDesde());
        }
        if (request.getVigenciaHasta() != null) {
            plan.setVigenciaHasta(request.getVigenciaHasta());
        }

        return mapper.convert(planRepository.saveAndFlush(plan));
    }

    @Transactional
    public PlanMembresiaModel cambiarEstado(Integer id, boolean activo) {
        PlanMembresiaEntity plan = obtenerPlan(id);
        plan.setActivo(activo);
        return mapper.convert(planRepository.saveAndFlush(plan));
    }

    @Transactional
    public PlanMembresiaModel cambiarEstado(Integer id, CambiarEstadoRequest request) {
        return cambiarEstado(id, Boolean.TRUE.equals(request.getActivo()));
    }

    @Transactional
    public void eliminarPlan(Integer id) {
        PlanMembresiaEntity plan = obtenerPlan(id);
        try {
            planRepository.delete(plan);
            planRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar este plan porque ya tiene tarifas o suscripciones vendidas asociadas. Puedes desactivarlo usando el botón de estado.");
        }
    }

    @Transactional
    public PlanSedeModel asignarOActualizarPrecioSede(Integer planId, AsignarPrecioSedeRequest request) {
        PlanMembresiaEntity plan = obtenerPlan(planId);
        SedeEntity sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada con id: " + request.getSedeId()));

        PlanSedeEntity planSede = planSedeRepository.findByPlanIdAndSedeId(planId, request.getSedeId())
                .orElseGet(() -> PlanSedeEntity.builder()
                        .plan(plan)
                        .sede(sede)
                        .build());

        planSede.setPrecioActual(request.getPrecioActual());
        planSede.setRecargoMultisede(request.getRecargoMultisede() != null ? request.getRecargoMultisede() : BigDecimal.ZERO);
        if (request.getActivo() != null) {
            planSede.setActivo(request.getActivo());
        }

        return mapper.convert(planSedeRepository.saveAndFlush(planSede));
    }

    @Transactional(readOnly = true)
    public List<PlanSedeModel> obtenerPreciosPorPlan(Integer planId) {
        obtenerPlan(planId);
        return mapper.convertPrecios(planSedeRepository.findByPlanId(planId));
    }

    @Transactional
    public List<PlanSedeModel> obtenerPlanesPorSede(Integer sedeId, Boolean soloActivos) {
        SedeEntity sede = sedeRepository.findById(sedeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada con id: " + sedeId));

        List<PlanSedeEntity> lista = Boolean.TRUE.equals(soloActivos)
                ? planSedeRepository.findBySedeIdAndActivo(sedeId, true)
                : planSedeRepository.findBySedeId(sedeId);

        if (lista.isEmpty()) {
            List<PlanMembresiaEntity> planesActivos = planRepository.findByActivo(true);
            List<PlanSedeEntity> autoPlanes = new ArrayList<>();
            for (PlanMembresiaEntity p : planesActivos) {
                BigDecimal precio = planSedeRepository.findByPlanId(p.getId()).stream()
                        .map(PlanSedeEntity::getPrecioActual)
                        .filter(java.util.Objects::nonNull)
                        .findFirst()
                        .orElse(BigDecimal.valueOf(120.00));
                PlanSedeEntity nuevo = PlanSedeEntity.builder()
                        .plan(p)
                        .sede(sede)
                        .precioActual(precio)
                        .recargoMultisede(BigDecimal.ZERO)
                        .activo(true)
                        .build();
                autoPlanes.add(planSedeRepository.save(nuevo));
            }
            if (!autoPlanes.isEmpty()) {
                planSedeRepository.flush();
                return mapper.convertPrecios(autoPlanes);
            }
        }
        return mapper.convertPrecios(lista);
    }

    private PlanMembresiaEntity obtenerPlan(Integer id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan de membresía no encontrado con id: " + id));
    }

    private void validarUnicidadNombre(String nombre, Integer idActual) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        boolean duplicado = idActual == null
                ? planRepository.existsByNombre(nombre.trim())
                : planRepository.existsByNombreAndIdNot(nombre.trim(), idActual);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Regla de negocio: ya existe un plan con el nombre " + nombre);
        }
    }

    private void validarRangoVigencia(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin de vigencia no puede ser anterior a la fecha de inicio");
        }
    }
}
