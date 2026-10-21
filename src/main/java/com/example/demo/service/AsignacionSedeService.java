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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El empleado está inactivo");
        }
        SedeEntity sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sede no encontrada con id: " + request.getSedeId()));
        if (!sede.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sede está inactiva");
        }

        LocalDate desde = request.getFechaDesde() != null ? request.getFechaDesde() : LocalDate.now();
        LocalDate hasta = request.getFechaHasta();
        if (hasta != null && hasta.isBefore(desde)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha hasta no puede ser anterior a la fecha desde");
        }

        if (asignacionRepository.existsByEmpleadoIdAndSedeIdAndFechaDesde(empleado.getId(), sede.getId(), desde)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Regla de negocio: ya existe una asignación del empleado a la sede con esa fecha de inicio");
        }

        boolean cruce = asignacionRepository
                .findByEmpleadoIdAndSedeIdAndAnulada(empleado.getId(), sede.getId(), false).stream()
                .anyMatch(a -> (a.getFechaHasta() == null || !a.getFechaHasta().isBefore(desde))
                        && (hasta == null || !hasta.isBefore(a.getFechaDesde())));
        if (cruce) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Regla de negocio: el empleado ya tiene una asignación vigente en esa sede para ese periodo");
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La asignación está anulada");
        }
        LocalDate hasta = request != null && request.getFechaHasta() != null
                ? request.getFechaHasta() : LocalDate.now();
        if (hasta.isBefore(asignacion.getFechaDesde())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha hasta no puede ser anterior a la fecha desde");
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
