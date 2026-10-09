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
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

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
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}