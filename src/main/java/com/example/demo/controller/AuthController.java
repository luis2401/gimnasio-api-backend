package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegistrarUsuarioRequest;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.example.demo.entity.UsuarioEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registrar(@Valid @RequestBody RegistrarUsuarioRequest registrarUsuarioRequest) {
        return authService.registrar(registrarUsuarioRequest);
    }

    /** Revoca la sesión del token con el que se hace la petición. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        authService.logout(token);
    }

    /** Revoca todas las sesiones del usuario autenticado (cierra sesión en todos los dispositivos). */
    @PostMapping("/logout-todas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutTodas(Authentication authentication) {
        authService.logoutTodas((UsuarioEntity) authentication.getPrincipal());
    }
}

