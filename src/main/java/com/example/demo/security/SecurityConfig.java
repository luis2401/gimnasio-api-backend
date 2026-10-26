package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(manejador -> manejador
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"mensaje\":\"No autenticado: se requiere un token valido\"}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"mensaje\":\"Acceso denegado: no tiene el rol requerido\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/gimnasio", "/gimnasio/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/gimnasio/reporte", "/gimnasio/reporte/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers("/gimnasio", "/gimnasio/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/sedes", "/sedes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sedes", "/sedes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/sedes", "/sedes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/sedes", "/sedes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/sedes", "/sedes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/planes", "/planes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/planes", "/planes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/planes", "/planes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/planes", "/planes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/planes", "/planes/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/pagos/*/anular").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/pagos/reporte").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/suscripciones/*/estado").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers("/pagos", "/pagos/**", "/suscripciones", "/suscripciones/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/empleados/instructores").authenticated()
                        .requestMatchers("/empleados", "/empleados/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/clases", "/clases/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/clases", "/clases/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/clases", "/clases/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/clases", "/clases/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers("/inscripciones", "/inscripciones/**", "/asistencias", "/asistencias/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/asignaciones", "/dashboard").authenticated()
                        .requestMatchers("/asignaciones", "/asignaciones/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers("/usuarios", "/usuarios/**").hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers( "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**" ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @org.springframework.beans.factory.annotation.Value(
                    "${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://yanfitnes.vercel.app,https://yanfitness.vercel.app}")
            List<String> origenesPermitidos) {
        CorsConfiguration configuration = new CorsConfiguration();
        java.util.List<String> patrones = new java.util.ArrayList<>(origenesPermitidos);
        for (String extra : List.of(
                "https://yanfitnes.vercel.app",
                "https://yanfitness.vercel.app",
                "https://*.vercel.app",
                "http://localhost:3000",
                "http://localhost:5173"
        )) {
            if (!patrones.contains(extra)) {
                patrones.add(extra);
            }
        }
        configuration.setAllowedOriginPatterns(patrones);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}