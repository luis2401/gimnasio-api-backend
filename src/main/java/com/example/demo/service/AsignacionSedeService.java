package com.example.demo.service;

import com.example.demo.Model.AsignacionSedeMapping;
import com.example.demo.Model.AsignacionSedeModel;
import com.example.demo.dto.CreateAsignacionSedeRequest;
import com.example.demo.dto.FinalizarAsignacionRequest;
import com.example.demo.entity.AsignacionSedeEntity;
import com.example.demo.entity.EmpleadoEntity;
import com.example.demo.entity.SedeEntity;
import com.example.demo.repository.AsignacionSedeRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AsignacionSedeService {

    @Autowired
    private AsignacionSedeRepository asignacionRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private AsignacionSedeMapping mapper;

    @Transactional(readOnly = true)
    public List<AsignacionSedeModel> buscar(Integer empleadoId, Integer sedeId) {
        if (empleadoId != null) {
            return mapper.convert(asignacionRepository.findByEmpleadoIdOrderByFechaDesdeDesc(empleadoId));
        }
        if (sedeId != null) {
            return mapper.convert(asignacionRepository.findBySedeIdOrderByFechaDesdeDesc(sedeId));
        }
        return mapper.convert(asignacionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public AsignacionSedeModel buscarPorId(Integer id) {
        return mapper.convert(obtener(id));
    }

    @Transactional
    public AsignacionSedeModel asignar(CreateAsignacionSedeRequest request) {
        EmpleadoEntity empleado = empleadoRepository.findById(request.getEmpleadoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado con id: " + request.getEmpleadoId()));
        if (!empleado.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El colaborador seleccionado se encuentra inactivo");
        }
        String rolCod = empleado.getRol() != null && empleado.getRol().getCodigo() != null
                ? empleado.getRol().getCodigo().toUpperCase() : "";
        if ("ADMINISTRADOR".equals(rolCod) || "ADMIN".equals(rolCod) || "SUPERVISOR".equals(rolCod)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los roles Administrador y Supervisor tienen alcance global en todas las sedes y no se asignan a una sede específica");
        }

        SedeEntity sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sede no encontrada con id: " + request.getSedeId()));
        if (!sede.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sede seleccionada se encuentra inactiva");
        }

        LocalDate hoy = LocalDate.now();
        LocalDate desde = request.getFechaDesde() != null ? request.getFechaDesde() : hoy;
        LocalDate hasta = request.getFechaHasta();
        if (hasta != null && hasta.isBefore(desde)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha límite no puede ser anterior a la fecha de inicio");
        }

        // Si ya existe un registro con la misma fechaDesde pero estaba anulado o ya finalizado (fechaHasta <= hoy), lo reactivamos/actualizamos
        java.util.Optional<AsignacionSedeEntity> mismaFechaOpt = asignacionRepository
                .findByEmpleadoIdAndSedeIdAndFechaDesde(empleado.getId(), sede.getId(), desde);
        if (mismaFechaOpt.isPresent()) {
            AsignacionSedeEntity existente = mismaFechaOpt.get();
            boolean estabaCerrada = existente.isAnulada()
                    || (existente.getFechaHasta() != null && !existente.getFechaHasta().isAfter(hoy));
            if (estabaCerrada) {
                existente.setAnulada(false);
                existente.setFechaHasta(hasta);
                return mapper.convert(asignacionRepository.saveAndFlush(existente));
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El colaborador ya tiene una asignación vigente en esa sede con esa fecha de inicio");
        }

        // Ignorar asignaciones que ya fueron finalizadas (fechaHasta <= hoy) al evaluar cruce
        boolean cruce = asignacionRepository
                .findByEmpleadoIdAndSedeIdAndAnulada(empleado.getId(), sede.getId(), false).stream()
                .filter(a -> a.getFechaHasta() == null || a.getFechaHasta().isAfter(hoy))
                .anyMatch(a -> (a.getFechaHasta() == null || !a.getFechaHasta().isBefore(desde))
                        && (hasta == null || !hasta.isBefore(a.getFechaDesde())));
        if (cruce) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El colaborador ya cuenta con una asignación vigente activa en esa sede para ese periodo");
        }

        return mapper.convert(asignacionRepository.saveAndFlush(AsignacionSedeEntity.builder()
                .empleado(empleado)
                .sede(sede)
                .fechaDesde(desde)
                .fechaHasta(hasta)
                .anulada(false)
                .build()));
    }

    @Transactional
    public AsignacionSedeModel finalizar(Integer id, FinalizarAsignacionRequest request) {
        AsignacionSedeEntity asignacion = obtener(id);
        if (asignacion.isAnulada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La asignación ya se encuentra anulada");
        }
        LocalDate hoy = LocalDate.now();
        LocalDate hasta = request != null && request.getFechaHasta() != null
                ? request.getFechaHasta() : hoy;
        if (hasta.isBefore(asignacion.getFechaDesde())) {
            hasta = asignacion.getFechaDesde();
        }
        asignacion.setFechaHasta(hasta);
        return mapper.convert(asignacionRepository.saveAndFlush(asignacion));
    }

    @Transactional
    public AsignacionSedeModel anular(Integer id) {
        AsignacionSedeEntity asignacion = obtener(id);
        if (asignacion.isAnulada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La asignación ya está anulada");
        }
        asignacion.setAnulada(true);
        return mapper.convert(asignacionRepository.saveAndFlush(asignacion));
    }

    private AsignacionSedeEntity obtener(Integer id) {
        return asignacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Asignación no encontrada con id: " + id));
    }
}
