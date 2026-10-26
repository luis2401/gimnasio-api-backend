package com.example.demo.repository;

import com.example.demo.entity.SocioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SocioRepository extends JpaRepository<SocioEntity, Long> {

    java.util.Optional<SocioEntity> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    List<SocioEntity> findByUsuarioId(Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    long countByActivo(boolean activo);

    long countByFechaCreacionAfter(Date fecha);

    org.springframework.data.domain.Page<SocioEntity>
    findByDniContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
            String dni, String nombre, String apellido, org.springframework.data.domain.Pageable pageable);
}