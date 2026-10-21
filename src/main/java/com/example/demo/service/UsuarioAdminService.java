package com.example.demo.service;

import com.example.demo.Model.PaginaModel;
import com.example.demo.Model.SesionModel;
import com.example.demo.Model.UsuarioModel;
import com.example.demo.dto.BloquearUsuarioRequest;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.entity.UsuarioEntity;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

/** Administración de usuarios del sistema: listado, bloqueo/desbloqueo, activación, reset de clave y sesiones. */
@Service
public class UsuarioAdminService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SesionService sesionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PaginaModel<UsuarioModel> buscarUsuarios(String q, int page, int size) {
        Pageable pageable = Paginacion.de(page, size, Sort.by("username").ascending());
        Page<UsuarioEntity> pagina = q != null && !q.isBlank()
                ? usuarioRepository.findByUsernameContainingIgnoreCase(q.trim(), pageable)
                : usuarioRepository.findAll(pageable);
        return PaginaModel.from(pagina.map(this::aModelo));
    }

    @Transactional(readOnly = true)
    public UsuarioModel buscarPorId(Long id) {
        return aModelo(obtener(id));
    }

    @Transactional
    public UsuarioModel cambiarEstado(Long id, CambiarEstadoRequest request) {
        UsuarioEntity usuario = obtener(id);
        boolean activo = Boolean.TRUE.equals(request.getActivo());
        if (!activo) {
            evitarAutoAccion(usuario, "desactivar su propia cuenta");
        }
        usuario.setActivo(activo);
        if (!activo) {
            sesionService.revocarTodas(usuario.getId());
        }
        return aModelo(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional
    public UsuarioModel bloquear(Long id, BloquearUsuarioRequest request) {
        UsuarioEntity usuario = obtener(id);
        evitarAutoAccion(usuario, "bloquear su propia cuenta");
        usuario.setBloqueadoHasta(new Date(System.currentTimeMillis() + request.getMinutos() * 60_000L));
        sesionService.revocarTodas(usuario.getId());
        return aModelo(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional
    public UsuarioModel desbloquear(Long id) {
        UsuarioEntity usuario = obtener(id);
        usuario.setBloqueadoHasta(null);
        usuario.setIntentosFallidos(0);
        usuario.setNivelBloqueo(0);
        return aModelo(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional
    public void resetearPassword(Long id, ResetPasswordRequest request) {
        UsuarioEntity usuario = obtener(id);
        usuario.setPassword(passwordEncoder.encode(request.getNuevoPassword()));
        usuario.setIntentosFallidos(0);
        usuarioRepository.saveAndFlush(usuario);
        sesionService.revocarTodas(usuario.getId());
    }

    @Transactional(readOnly = true)
    public List<SesionModel> sesionesActivas(Long id) {
        obtener(id);
        return sesionService.listarActivas(id);
    }

    @Transactional
    public int cerrarSesiones(Long id) {
        obtener(id);
        return sesionService.revocarTodas(id);
    }

    private UsuarioEntity obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con id: " + id));
    }

    private void evitarAutoAccion(UsuarioEntity objetivo, String accion) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioEntity actual
                && actual.getId().equals(objetivo.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puede " + accion);
        }
    }

    private UsuarioModel aModelo(UsuarioEntity u) {
        return UsuarioModel.builder()
                .id(u.getId())
                .username(u.getUsername())
                .empleadoId(u.getEmpleado() != null ? u.getEmpleado().getId() : null)
                .empleadoNombre(u.getEmpleado() != null
                        ? u.getEmpleado().getNombre() + " " + u.getEmpleado().getApellido() : null)
                .rolCodigo(u.getRolCodigo())
                .activo(u.isActivo())
                .intentosFallidos(u.getIntentosFallidos())
                .nivelBloqueo(u.getNivelBloqueo())
                .bloqueadoHasta(u.getBloqueadoHasta())
                .bloqueado(!u.isAccountNonLocked())
                .ultimoAcceso(u.getUltimoAcceso())
                .fechaCreacion(u.getFechaCreacion())
                .build();
    }
}
