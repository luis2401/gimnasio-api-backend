package com.example.demo.service;

import com.example.demo.Model.AsistenciaClaseModel;
import com.example.demo.Model.ClaseMapping;
import com.example.demo.dto.RegistrarAsistenciaRequest;
import com.example.demo.entity.AsistenciaClaseEntity;
import com.example.demo.entity.SocioClaseEntity;
import com.example.demo.repository.AsistenciaClaseRepository;
import com.example.demo.repository.ClaseRepository;
import com.example.demo.repository.SocioClaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaClaseRepository asistenciaRepository;

    @Autowired
    private SocioClaseRepository socioClaseRepository;

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private ClaseMapping mapper;

    /** Registra la asistencia del día; si ya existía para esa fecha, la actualiza. */
    @Transactional
    public AsistenciaClaseModel registrar(RegistrarAsistenciaRequest request) {
        SocioClaseEntity inscripcion = socioClaseRepository.findById(request.getSocioClaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inscripción no encontrada con id: " + request.getSocioClaseId()));

        LocalDate fecha = request.getFecha() != null ? request.getFecha() : LocalDate.now();

        if (fecha.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede registrar asistencia en una fecha futura");
        }
        if (fecha.isBefore(inscripcion.getFechaInscripcion())
                || (inscripcion.getFechaBaja() != null && fecha.isAfter(inscripcion.getFechaBaja()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha está fuera del periodo de inscripción del socio");
        }
        if (fecha.getDayOfWeek().getValue() != inscripcion.getClase().getDiaSemana()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha no corresponde al día de la semana en que se dicta la clase");
        }

        AsistenciaClaseEntity asistencia = asistenciaRepository
                .findBySocioClaseIdAndFecha(inscripcion.getId(), fecha)
                .orElseGet(() -> AsistenciaClaseEntity.builder()
                        .socioClase(inscripcion)
                        .fecha(fecha)
                        .build());

        asistencia.setEstado(request.getEstado());
        asistencia.setObservacion(request.getObservacion());
        asistencia.setRegistradoPor(EmpleadoActual.obtener());

        return mapper.convert(asistenciaRepository.saveAndFlush(asistencia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaClaseModel> buscarPorClaseYFecha(Integer claseId, LocalDate fecha) {
        claseRepository.findById(claseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Clase no encontrada con id: " + claseId));
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return mapper.convertAsistencias(asistenciaRepository.findBySocioClaseClaseIdAndFecha(claseId, dia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaClaseModel> buscarPorInscripcion(Long socioClaseId) {
        socioClaseRepository.findById(socioClaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inscripción no encontrada con id: " + socioClaseId));
        return mapper.convertAsistencias(asistenciaRepository.findBySocioClaseIdOrderByFechaDesc(socioClaseId));
    }
}
