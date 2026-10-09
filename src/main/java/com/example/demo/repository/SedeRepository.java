package com.example.demo.repository;

import com.example.demo.entity.SedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<SedeEntity, Integer> {

    Optional<SedeEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Integer id);

    List<SedeEntity> findByActiva(boolean activa);

    long countByActiva(boolean activa);
}
