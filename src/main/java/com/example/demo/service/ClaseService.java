package com.example.demo.service;

import com.example.demo.Model.ClaseMapping;
import com.example.demo.Model.ClaseModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateClaseRequest;
import com.example.demo.dto.UpdateClaseRequest;
import com.example.demo.entity.ClaseEntity;
import com.example.demo.entity.EmpleadoEntity;
import com.example.demo.entity.SedeEntity;
import com.example.demo.repository.ClaseRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.SedeRepository;
import com.example.demo.repository.SocioClaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;

@Service
public class ClaseService {

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private SocioClaseRepository socioClaseRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private ClaseMapping mapper;

    @Transactional(readOnly = true)
    public List<ClaseModel> buscarClases(Integer sedeId, Integer instructorId, Boolean soloActivas) {
        List<ClaseEntity> lista;
        if (sedeId != null) {
            lista = claseRepository.findBySedeId(sedeId);
        } else if (instructorId != null) {
            lista = claseRepository.findByInstructorId(instructorId);
        } else {
            lista = claseRepository.findAll();
        }
        if (Boolean.TRUE.equals(soloActivas)) {
            lista = lista.stream().filter(ClaseEntity::isActivo).toList();
        }
        return lista.stream().map(this::aModelo).toList();
    }

    @Transactional(readOnly = true)
    public ClaseModel buscarPorId(Integer id) {
        return aModelo(obtener(id));
    }

    @Transactional
    public ClaseModel crearClase(CreateClaseRequest request) {
        validarHorario(request.getHoraInicio(), request.getHoraFin());
        EmpleadoEntity instructor = obtenerInstructor(request.getInstructorId());
        SedeEntity sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sede no encontrada con id: " + request.getSedeId()));
        if (!sede.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sede está inactiva");
        }
        Short dia = request.getDiaSemana().shortValue();
        validarCruceInstructor(instructor.getId(), dia, request.getHoraInicio(), request.getHoraFin(), null);

        ClaseEntity clase = claseRepository.saveAndFlush(ClaseEntity.builder()
                .nombre(request.getNombre().trim())
                .diaSemana(dia)
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .cupoMax(request.getCupoMax())
                .instructor(instructor)
                .sede(sede)
                .activo(true)
                .build());
        return aModelo(clase);
    }

    @Transactional
    public ClaseModel updateClase(Integer id, UpdateClaseRequest request) {
        ClaseEntity clase = obtener(id);

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            clase.setNombre(request.getNombre().trim());
        }
        if (request.getDiaSemana() != null) {
            clase.setDiaSemana(request.getDiaSemana().shortValue());
        }
        if (request.getHoraInicio() != null) {
            clase.setHoraInicio(request.getHoraInicio());
        }
        if (request.getHoraFin() != null) {
            clase.setHoraFin(request.getHoraFin());
        }
        if (request.getCupoMax() != null) {
            long inscritos = socioClaseRepository.countByClaseIdAndActivo(id, true);
            if (request.getCupoMax() < inscritos) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El cupo no puede ser menor a los " + inscritos + " socios inscritos");
            }
            clase.setCupoMax(request.getCupoMax());
        }
        if (request.getInstructorId() != null) {
            clase.setInstructor(obtenerInstructor(request.getInstructorId()));
        }

        validarHorario(clase.getHoraInicio(), clase.getHoraFin());
        if (clase.isActivo()) {
            validarCruceInstructor(clase.getInstructor().getId(), clase.getDiaSemana(),
                    clase.getHoraInicio(), clase.getHoraFin(), id);
        }
        return aModelo(claseRepository.saveAndFlush(clase));
    }

    @Transactional
    public ClaseModel cambiarEstado(Integer id, CambiarEstadoRequest request) {
        ClaseEntity clase = obtener(id);
        boolean activo = Boolean.TRUE.equals(request.getActivo());
        if (activo) {
            validarCruceInstructor(clase.getInstructor().getId(), clase.getDiaSemana(),
                    clase.getHoraInicio(), clase.getHoraFin(), id);
        }
        clase.setActivo(activo);
        return aModelo(claseRepository.saveAndFlush(clase));
    }

    private ClaseModel aModelo(ClaseEntity clase) {
        ClaseModel modelo = mapper.convert(clase);
        modelo.setInscritos(socioClaseRepository.countByClaseIdAndActivo(clase.getId(), true));
        return modelo;
    }

    private ClaseEntity obtener(Integer id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Clase no encontrada con id: " + id));
    }

    private EmpleadoEntity obtenerInstructor(Integer instructorId) {
        EmpleadoEntity instructor = empleadoRepository.findById(instructorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado con id: " + instructorId));
        if (!instructor.isActivo() || !"INSTRUCTOR".equalsIgnoreCase(instructor.getRol().getCodigo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El empleado debe estar activo y tener el rol INSTRUCTOR");
        }
        return instructor;
    }

    private void validarHorario(LocalTime inicio, LocalTime fin) {
        if (inicio != null && fin != null && !fin.isAfter(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private void validarCruceInstructor(Integer instructorId, Short dia, LocalTime inicio, LocalTime fin, Integer idActual) {
        boolean cruce = claseRepository.findByInstructorIdAndDiaSemanaAndActivo(instructorId, dia, true).stream()
                .filter(c -> idActual == null || !c.getId().equals(idActual))
                .anyMatch(c -> inicio.isBefore(c.getHoraFin()) && fin.isAfter(c.getHoraInicio()));
        if (cruce) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Regla de negocio: el instructor ya tiene una clase en ese horario");
        }
    }
}
