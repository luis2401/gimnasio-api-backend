package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "usuario_sistema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoEntity empleado;

    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 40)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "intentos_fallidos")
    @Builder.Default
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    @Temporal(TemporalType.TIMESTAMP)
    private Date bloqueadoHasta;

    @Column(name = "ultimo_acceso")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ultimoAcceso;

    @Column(name = "creado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "actualizado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualizadoEn;

    @Column(name = "nivel_bloqueo")
    @Builder.Default
    private Integer nivelBloqueo = 0;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
        if (actualizadoEn == null) {
            actualizadoEn = new Date();
        }
    }

    @PreUpdate
    void preUpdate() {
        actualizadoEn = new Date();
    }

    public String getRolCodigo() {
        if (empleado != null && empleado.getRol() != null && empleado.getRol().getCodigo() != null) {
            return empleado.getRol().getCodigo();
        }
        return "RECEPCIONISTA";
    }

    public RolUsuario getRol() {
        String codigo = getRolCodigo();
        try {
            return RolUsuario.valueOf(codigo.toUpperCase());
        } catch (Exception e) {
            if ("ADMINISTRADOR".equalsIgnoreCase(codigo)) {
                return RolUsuario.ADMIN;
            }
            return RolUsuario.EMPLEADO;
        }
    }

    public void setRol(RolUsuario rolUsuario) {
        if (rolUsuario != null && empleado != null && empleado.getRol() != null) {
            empleado.getRol().setCodigo(rolUsuario.name());
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String codigo = getRolCodigo();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + codigo.toUpperCase()));
        if ("ADMINISTRADOR".equalsIgnoreCase(codigo)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return bloqueadoHasta == null || bloqueadoHasta.before(new Date());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}