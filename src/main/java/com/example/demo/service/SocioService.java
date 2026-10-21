package com.example.demo.service;

import com.example.demo.Model.SocioMapping;
import com.example.demo.Model.SocioModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateSocioRequest;
import com.example.demo.dto.ReporteSocio;
import com.example.demo.dto.UpdateSocioRequest;
import com.example.demo.entity.RolUsuario;
import com.example.demo.entity.SocioEntity;
import com.example.demo.entity.UsuarioEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.repository.SocioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private SocioMapping mapper;

    @Transactional(readOnly = true)
    public List<SocioModel> buscarSocios() {
        return mapper.convert(socioRepository.findAll());
    }

    @Transactional(readOnly = true)
    public com.example.demo.Model.PaginaModel<SocioModel> buscarSociosPagina(String q, int page, int size) {
        org.springframework.data.domain.Pageable pageable = Paginacion.de(page, size,
                org.springframework.data.domain.Sort.by("id").descending());
        org.springframework.data.domain.Page<SocioEntity> pagina = q != null && !q.isBlank()
                ? socioRepository.findByDniContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
                        q.trim(), q.trim(), q.trim(), pageable)
                : socioRepository.findAll(pageable);
        return com.example.demo.Model.PaginaModel.from(pagina.map(e -> mapper.convert(e)));
    }

    @Transactional(readOnly = true)
    public long cantidadSocios() {
        return socioRepository.count();
    }

    @Transactional
    public SocioModel crearSocio(CreateSocioRequest request) {
        validarUnicidadDni(request.getDni(), null);
        validarUnicidadCorreo(request.getCorreo(), null);

        SocioEntity socio = mapper.convert(request);
        socio.setUsuario(usuarioActual());
        socio.setFechaCreacion(new Date());
        return mapper.convert(socioRepository.saveAndFlush(socio));
    }

    @Transactional
    public SocioModel crearSocio(SocioModel model) {
        validarUnicidadDni(model.getDni(), null);
        validarUnicidadCorreo(model.getCorreo(), null);

        SocioEntity socio = mapper.convert(model);
        socio.setUsuario(usuarioActual());
        socio.setFechaCreacion(new Date());
        return mapper.convert(socioRepository.saveAndFlush(socio));
    }

    @Transactional(readOnly = true)
    public SocioModel buscarSocioPorId(Long id) {
        SocioEntity socio = obtenerSocio(id);
        return mapper.convert(socio);
    }

    @Transactional
    public SocioModel updateSocio(Long id, UpdateSocioRequest request) {
        SocioEntity socio = obtenerSocio(id);

        if (request.getCorreo() != null) {
            validarUnicidadCorreo(request.getCorreo(), id);
            socio.setCorreo(request.getCorreo());
        }
        if (request.getTelefono() != null) {
            socio.setTelefono(request.getTelefono());
        }
        return mapper.convert(socioRepository.saveAndFlush(socio));
    }

    @Transactional
    public void eliminarSocio(Long id) {
        SocioEntity socio = obtenerSocio(id);
        socioRepository.delete(socio);
    }

    @Transactional
    public SocioModel cambiarEstado(Long id, boolean activo) {
        SocioEntity socio = obtenerSocio(id);
        socio.setActivo(activo);
        return mapper.convert(socioRepository.saveAndFlush(socio));
    }

    @Transactional
    public SocioModel cambiarEstado(Long id, CambiarEstadoRequest request) {
        return cambiarEstado(id, Boolean.TRUE.equals(request.getActivo()));
    }

    @Transactional(readOnly = true)
    public ReporteSocio generarReporte() {
        LocalDate hoy = LocalDate.now();
        Date inicioDelMes = Date.from(hoy.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        return ReporteSocio.builder()
                .total(socioRepository.count())
                .activos(socioRepository.countByActivo(true))
                .inactivos(socioRepository.countByActivo(false))
                .creadosEsteMes(socioRepository.countByFechaCreacionAfter(inicioDelMes))
                .build();
    }

    private SocioEntity obtenerSocio(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Socio no encontrado con id: " + id));
    }

    private void validarUnicidadDni(String dni, Long idActual) {
        if (dni == null || dni.isBlank()) {
            return;
        }
        boolean duplicado = idActual == null
                ? socioRepository.existsByDni(dni)
                : socioRepository.existsByDniAndIdNot(dni, idActual);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Regla de negocio: ya existe un socio con el DNI " + dni);
        }
    }

    private void validarUnicidadCorreo(String correo, Long idActual) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        boolean duplicado = idActual == null
                ? socioRepository.existsByCorreo(correo)
                : socioRepository.existsByCorreoAndIdNot(correo, idActual);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Regla de negocio: ya existe un socio con el correo " + correo);
        }
    }

    private UsuarioEntity usuarioActual() {
        return (UsuarioEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean esAdministrador(UsuarioEntity usuario) {
        if (usuario == null) {
            return false;
        }
        String rol = usuario.getRolCodigo();
        return "ADMINISTRADOR".equalsIgnoreCase(rol)
                || "ADMIN".equalsIgnoreCase(rol)
                || usuario.getRol() == RolUsuario.ADMINISTRADOR
                || usuario.getRol() == RolUsuario.ADMIN;
    }
}