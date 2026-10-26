package com.example.demo.repository;

import com.example.demo.entity.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @EntityGraph(attributePaths = {"empleado", "empleado.rol"})
    Optional<UsuarioEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<UsuarioEntity> findByEmpleadoId(Integer empleadoId);

    @EntityGraph(attributePaths = {"empleado", "empleado.rol"})
    Page<UsuarioEntity> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"empleado", "empleado.rol"})
    Page<UsuarioEntity> findAll(Pageable pageable);
}