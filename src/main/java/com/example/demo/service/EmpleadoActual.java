package com.example.demo.service;

import com.example.demo.entity.EmpleadoEntity;
import com.example.demo.entity.UsuarioEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/** Utilidad para obtener el empleado (vendedor) del usuario autenticado. */
final class EmpleadoActual {

    private EmpleadoActual() {
    }

    static EmpleadoEntity obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioEntity usuario) || usuario.getEmpleado() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario autenticado no tiene un empleado asociado");
        }
        return usuario.getEmpleado();
    }
}
