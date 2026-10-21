package com.example.demo.repository;

import com.example.demo.entity.EmpleadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<EmpleadoEntity, Integer> {
    Optional<EmpleadoEntity> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Integer id);

    List<EmpleadoEntity> findByActivo(boolean activo);

    long countByActivo(boolean activo);

    List<EmpleadoEntity> findByRolCodigoAndActivo(String rolCodigo, boolean activo);
}
