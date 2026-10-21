package com.example.demo.security;

import com.example.demo.entity.UsuarioEntity;
import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.SesionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final SesionService sesionService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService,
                         SesionService sesionService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.sesionService = sesionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && !header.isBlank()) {
            String token = extraerToken(header);
            if (token != null) {
                try {
                    String username = jwtService.extraerUsername(token);
                    if (username != null
                            && SecurityContextHolder.getContext().getAuthentication() == null
                            && jwtService.esValido(token)
                            && sesionVigente(token)) {
                        UsuarioEntity usuario = (UsuarioEntity) userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken autenticacion =
                                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    }
                } catch (Exception ex) {
                    log.warn("No se pudo autenticar el token JWT en {}: {}", request.getRequestURI(), ex.getMessage());
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.debug("Header Authorization presente pero sin formato reconocible: {}", header);
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Tokens con jti deben tener una sesión activa; los anteriores (sin jti) se siguen aceptando. */
    private boolean sesionVigente(String token) {
        return jwtService.extraerJti(token) == null || sesionService.esSesionActiva(token);
    }

    private String extraerToken(String header) {
        String token = header.trim();

        while (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
            token = token.substring(1, token.length() - 1).trim();
        }

        while (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        return token.isEmpty() ? null : token;
    }
}