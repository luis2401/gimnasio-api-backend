package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegistrarUsuarioRequest;
import com.example.demo.entity.EmpleadoEntity;
import com.example.demo.entity.RolEntity;
import com.example.demo.entity.UsuarioEntity;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UsuarioEntity usuario;
        try {
            usuario = userDetailsService.obtenerPorUsername(request.getUsername());
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        if (!usuario.isEnabled() || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuario);
        return LoginResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .rol(usuario.getRolCodigo())
                .build();
    }

    @Transactional
    public LoginResponse registrar(RegistrarUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }

        String rolCodigo = request.getRol() != null ? request.getRol().name() : "RECEPCIONISTA";
        if ("ADMIN".equalsIgnoreCase(rolCodigo)) {
            rolCodigo = "ADMINISTRADOR";
        } else if ("EMPLEADO".equalsIgnoreCase(rolCodigo)) {
            rolCodigo = "RECEPCIONISTA";
        }

        String finalRolCodigo = rolCodigo;
        RolEntity rolEntity = rolRepository.findByCodigo(finalRolCodigo)
                .orElseGet(() -> rolRepository.findAll().stream().findFirst().orElse(null));

        if (rolEntity == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se encontró el rol especificado en el sistema");
        }

        String dniGenerado = String.format("%08d", Math.abs((request.getUsername() + System.currentTimeMillis()).hashCode() % 100000000));
        EmpleadoEntity empleado = EmpleadoEntity.builder()
                .dni(dniGenerado)
                .nombre(request.getUsername())
                .apellido("Sistema")
                .rol(rolEntity)
                .telefono("999999999")
                .activo(true)
                .build();
        empleado = empleadoRepository.save(empleado);

        UsuarioEntity usuario = UsuarioEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .empleado(empleado)
                .activo(true)
                .fechaCreacion(new Date())
                .build();
        usuarioRepository.save(usuario);

        String token = jwtService.generarToken(usuario);
        return LoginResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .rol(usuario.getRolCodigo())
                .build();
    }
}