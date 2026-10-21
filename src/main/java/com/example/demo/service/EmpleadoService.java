package com.example.demo.service;

import com.example.demo.Model.EmpleadoMapping;
import com.example.demo.Model.EmpleadoModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateEmpleadoRequest;
import com.example.demo.dto.UpdateEmpleadoRequest;
import com.example.demo.entity.EmpleadoEntity;
import com.example.demo.entity.RolEntity;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EmpleadoMapping mapper;

    @Transactional(readOnly = true)
    public List<EmpleadoModel> buscarEmpleados(Boolean soloActivos) {
        return mapper.convert(Boolean.TRUE.equals(soloActivos)
                ? empleadoRepository.findByActivo(true)
                : empleadoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public com.example.demo.Model.PaginaModel<EmpleadoModel> buscarPagina(Boolean soloActivos, int page, int size) {
        org.springframework.data.domain.Pageable pageable = Paginacion.de(page, size,
                org.springframework.data.domain.Sort.by("apellido").ascending().and(
                        org.springframework.data.domain.Sort.by("nombre").ascending()));
        org.springframework.data.domain.Page<EmpleadoEntity> pagina = Boolean.TRUE.equals(soloActivos)
                ? empleadoRepository.findByActivo(true, pageable)
                : empleadoRepository.findAll(pageable);
        return com.example.demo.Model.PaginaModel.from(pagina.map(e -> mapper.convert(e)));
    }

    @Transactional(readOnly = true)
    public List<EmpleadoModel> buscarInstructores() {
        return mapper.convert(empleadoRepository.findByRolCodigoAndActivo("INSTRUCTOR", true));
    }

    @Transactional(readOnly = true)
    public EmpleadoModel buscarPorId(Integer id) {
        return mapper.convert(obtener(id));
    }

    @Transactional
    public EmpleadoModel crearEmpleado(CreateEmpleadoRequest request) {
        if (empleadoRepository.existsByDni(request.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Regla de negocio: ya existe un empleado con el DNI " + request.getDni());
        }
        RolEntity rol = obtenerRol(request.getRolId());

        EmpleadoEntity empleado = EmpleadoEntity.builder()
                .dni(request.getDni())
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .rol(rol)
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .direccion(request.getDireccion())
                .especialidad(request.getEspecialidad())
                .activo(true)
                .build();
        return mapper.convert(empleadoRepository.saveAndFlush(empleado));
    }

    @Transactional
    public EmpleadoModel updateEmpleado(Integer id, UpdateEmpleadoRequest request) {
        EmpleadoEntity empleado = obtener(id);

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            empleado.setNombre(request.getNombre().trim());
        }
        if (request.getApellido() != null && !request.getApellido().isBlank()) {
            empleado.setApellido(request.getApellido().trim());
        }
        if (request.getRolId() != null) {
            empleado.setRol(obtenerRol(request.getRolId()));
        }
        if (request.getTelefono() != null) {
            empleado.setTelefono(request.getTelefono());
        }
        if (request.getCorreo() != null) {
            empleado.setCorreo(request.getCorreo());
        }
        if (request.getDireccion() != null) {
            empleado.setDireccion(request.getDireccion());
        }
        if (request.getEspecialidad() != null) {
            empleado.setEspecialidad(request.getEspecialidad());
        }
        return mapper.convert(empleadoRepository.saveAndFlush(empleado));
    }

    @Transactional
    public EmpleadoModel cambiarEstado(Integer id, CambiarEstadoRequest request) {
        EmpleadoEntity empleado = obtener(id);
        empleado.setActivo(Boolean.TRUE.equals(request.getActivo()));
        return mapper.convert(empleadoRepository.saveAndFlush(empleado));
    }

    private EmpleadoEntity obtener(Integer id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado con id: " + id));
    }

    private RolEntity obtenerRol(Short rolId) {
        RolEntity rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Rol no encontrado con id: " + rolId));
        if (!rol.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol está inactivo");
        }
        return rol;
    }
}
