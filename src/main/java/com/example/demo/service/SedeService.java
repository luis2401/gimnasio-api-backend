package com.example.demo.service;

import com.example.demo.Model.SedeMapping;
import com.example.demo.Model.SedeModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateSedeRequest;
import com.example.demo.dto.UpdateSedeRequest;
import com.example.demo.entity.SedeEntity;
import com.example.demo.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SedeService {

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private com.example.demo.repository.PlanMembresiaRepository planMembresiaRepository;

    @Autowired
    private com.example.demo.repository.PlanSedeRepository planSedeRepository;

    @Autowired
    private SedeMapping sedeMapping;

    @Transactional(readOnly = true)
    public List<SedeModel> buscarSedes() {
        return sedeMapping.convert(sedeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<SedeModel> buscarSedesActivas() {
        return sedeMapping.convert(sedeRepository.findByActiva(true));
    }

    @Transactional(readOnly = true)
    public SedeModel buscarSedePorId(Integer id) {
        return sedeMapping.convert(obtenerSede(id));
    }

    @Transactional
    public SedeModel crearSede(CreateSedeRequest request) {
        validarUnicidadCodigo(request.getCodigo(), null);
        validarUnicidadNombre(request.getNombre(), null);

        SedeEntity sede = sedeMapping.convert(request);
        if (request.getActiva() != null) {
            sede.setActiva(request.getActiva());
        }
        sede = sedeRepository.saveAndFlush(sede);

        for (com.example.demo.entity.PlanMembresiaEntity plan : planMembresiaRepository.findByActivo(true)) {
            if (planSedeRepository.findByPlanIdAndSedeId(plan.getId(), sede.getId()).isEmpty()) {
                java.math.BigDecimal precioDefecto = plan.getDuracionDias() <= 35
                        ? new java.math.BigDecimal("120.00")
                        : plan.getDuracionDias() <= 100
                        ? new java.math.BigDecimal("320.00")
                        : new java.math.BigDecimal("1100.00");
                planSedeRepository.saveAndFlush(com.example.demo.entity.PlanSedeEntity.builder()
                        .plan(plan)
                        .sede(sede)
                        .precioActual(precioDefecto)
                        .recargoMultisede(new java.math.BigDecimal("20.00"))
                        .activo(true)
                        .build());
            }
        }

        return sedeMapping.convert(sede);
    }

    @Transactional
    public SedeModel updateSede(Integer id, UpdateSedeRequest request) {
        SedeEntity sede = obtenerSede(id);

        if (request.getCodigo() != null && !request.getCodigo().isBlank()) {
            validarUnicidadCodigo(request.getCodigo(), id);
            sede.setCodigo(request.getCodigo().trim());
        }

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            validarUnicidadNombre(request.getNombre(), id);
            sede.setNombre(request.getNombre().trim());
        }

        if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
            sede.setDireccion(request.getDireccion().trim());
        }

        if (request.getActiva() != null) {
            sede.setActiva(request.getActiva());
        }

        return sedeMapping.convert(sedeRepository.saveAndFlush(sede));
    }

    @Transactional
    public SedeModel cambiarEstado(Integer id, boolean activa) {
        SedeEntity sede = obtenerSede(id);
        sede.setActiva(activa);
        return sedeMapping.convert(sedeRepository.saveAndFlush(sede));
    }

    @Transactional
    public SedeModel cambiarEstado(Integer id, CambiarEstadoRequest request) {
        return cambiarEstado(id, Boolean.TRUE.equals(request.getActivo()));
    }

    @Transactional
    public void eliminarSede(Integer id) {
        SedeEntity sede = obtenerSede(id);
        try {
            sedeRepository.delete(sede);
            sedeRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar esta sede porque cuenta con planes, pagos, clases o asignaciones vinculadas. Puedes desactivarla usando el botón de estado.");
        }
    }

    private SedeEntity obtenerSede(Integer id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada con id: " + id));
    }

    private void validarUnicidadCodigo(String codigo, Integer idActual) {
        if (codigo == null || codigo.isBlank()) {
            return;
        }
        boolean duplicado = idActual == null
                ? sedeRepository.existsByCodigo(codigo.trim())
                : sedeRepository.existsByCodigoAndIdNot(codigo.trim(), idActual);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Regla de negocio: ya existe una sede con el código " + codigo);
        }
    }

    private void validarUnicidadNombre(String nombre, Integer idActual) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        boolean duplicado = idActual == null
                ? sedeRepository.existsByNombre(nombre.trim())
                : sedeRepository.existsByNombreAndIdNot(nombre.trim(), idActual);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Regla de negocio: ya existe una sede con el nombre " + nombre);
        }
    }
}
