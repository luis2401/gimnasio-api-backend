package com.example.demo.service;

import com.example.demo.Model.ClaseMapping;
import com.example.demo.Model.SocioClaseModel;
import com.example.demo.dto.InscribirSocioRequest;
import com.example.demo.entity.ClaseEntity;
import com.example.demo.entity.EstadoSuscripcion;
import com.example.demo.entity.SocioClaseEntity;
import com.example.demo.entity.SocioEntity;
import com.example.demo.entity.SuscripcionEntity;
import com.example.demo.repository.ClaseRepository;
import com.example.demo.repository.SocioClaseRepository;
import com.example.demo.repository.SocioRepository;
import com.example.demo.repository.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionService {

    @Autowired
    private SocioClaseRepository socioClaseRepository;

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private ClaseMapping mapper;

    @Transactional(readOnly = true)
    public List<SocioClaseModel> buscarPorClase(Integer claseId, Boolean soloActivas) {
        claseRepository.findById(claseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Clase no encontrada con id: " + claseId));
        List<SocioClaseEntity> lista = Boolean.FALSE.equals(soloActivas)
                ? socioClaseRepository.findAll().stream().filter(i -> i.getClase().getId().equals(claseId)).toList()
                : socioClaseRepository.findByClaseIdAndActivo(claseId, true);
        return mapper.convertInscripciones(lista);
    }

    @Transactional(readOnly = true)
    public List<SocioClaseModel> buscarPorSocio(Long socioId, Boolean soloActivas) {
        return mapper.convertInscripciones(Boolean.TRUE.equals(soloActivas)
                ? socioClaseRepository.findBySocioIdAndActivo(socioId, true)
                : socioClaseRepository.findBySocioId(socioId));
    }

    @Transactional
    public SocioClaseModel inscribir(InscribirSocioRequest request) {
        ClaseEntity clase = claseRepository.findById(request.getClaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Clase no encontrada con id: " + request.getClaseId()));
        if (!clase.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clase está inactiva");
        }

        SocioEntity socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Socio no encontrado con id: " + request.getSocioId()));
        if (!socio.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El socio está inactivo");
        }

        java.util.Optional<SocioClaseEntity> yaInscrito = socioClaseRepository
                .findBySocioIdAndActivo(socio.getId(), true).stream()
                .filter(ic -> ic.getClase().getId().equals(clase.getId()))
                .findFirst();
        if (yaInscrito.isPresent()) {
            return mapper.convert(yaInscrito.get());
        }

        validarMembresia(socio.getId(), clase);

        if (socioClaseRepository.countByClaseIdAndActivo(clase.getId(), true) >= clase.getCupoMax()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La clase seleccionada ya alcanzó su cupo máximo de participantes");
        }

        SocioClaseEntity inscripcion = socioClaseRepository.saveAndFlush(SocioClaseEntity.builder()
                .socio(socio)
                .clase(clase)
                .fechaInscripcion(LocalDate.now())
                .activo(true)
                .build());
        return mapper.convert(inscripcion);
    }

    @Transactional
    public SocioClaseModel darDeBaja(Long id) {
        SocioClaseEntity inscripcion = socioClaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inscripción no encontrada con id: " + id));
        if (!inscripcion.isActivo()) {
            return mapper.convert(inscripcion);
        }
        inscripcion.setActivo(false);
        inscripcion.setFechaBaja(LocalDate.now());
        return mapper.convert(socioClaseRepository.saveAndFlush(inscripcion));
    }

    /** El socio necesita una membresía vigente que cubra la sede de la clase (o sea multisede). */
    private void validarMembresia(Long socioId, ClaseEntity clase) {
        LocalDate hoy = LocalDate.now();
        suscripcionRepository.marcarVencidas(hoy);
        List<SuscripcionEntity> activas = suscripcionRepository.findBySocioIdOrderByFechaInicioDesc(socioId).stream()
                .filter(s -> s.getEstado() == EstadoSuscripcion.ACTIVA && !s.getFechaFin().isBefore(hoy))
                .toList();

        if (activas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El socio no cuenta con una membresía vigente activa");
        }

        boolean cubreSede = activas.stream().anyMatch(s ->
                s.isAccesoMultisede() || s.getPlanSede().getSede().getId().equals(clase.getSede().getId()));
        if (!cubreSede) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La membresía del socio no incluye acceso a la sede de esta clase (requiere plan en la misma sede o acceso Multisede)");
        }
    }
}
