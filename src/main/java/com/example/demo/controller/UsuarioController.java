package com.example.demo.controller;

import com.example.demo.Model.PaginaModel;
import com.example.demo.Model.SesionModel;
import com.example.demo.Model.UsuarioModel;
import com.example.demo.dto.BloquearUsuarioRequest;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.UsuarioAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioAdminService usuarioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PaginaModel<UsuarioModel> obtenerUsuarios(@RequestParam(required = false) String q,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return usuarioService.buscarUsuarios(q, page, size);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UsuarioModel obtenerPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.OK)
    public UsuarioModel cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return usuarioService.cambiarEstado(id, request);
    }

    @PatchMapping("/{id}/bloquear")
    @ResponseStatus(HttpStatus.OK)
    public UsuarioModel bloquear(@PathVariable Long id, @Valid @RequestBody BloquearUsuarioRequest request) {
        return usuarioService.bloquear(id, request);
    }

    @PatchMapping("/{id}/desbloquear")
    @ResponseStatus(HttpStatus.OK)
    public UsuarioModel desbloquear(@PathVariable Long id) {
        return usuarioService.desbloquear(id);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetearPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        usuarioService.resetearPassword(id, request);
    }

    @GetMapping("/{id}/sesiones")
    @ResponseStatus(HttpStatus.OK)
    public List<SesionModel> sesionesActivas(@PathVariable Long id) {
        return usuarioService.sesionesActivas(id);
    }

    @DeleteMapping("/{id}/sesiones")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesiones(@PathVariable Long id) {
        usuarioService.cerrarSesiones(id);
    }
}
