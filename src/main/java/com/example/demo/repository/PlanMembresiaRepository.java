package com.example.demo.repository;

import com.example.demo.entity.PlanMembresiaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanMembresiaRepository extends JpaRepository<PlanMembresiaEntity, Integer> {

    Optional<PlanMembresiaEntity> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Integer id);

    List<PlanMembresiaEntity> findByActivo(boolean activo);

    long countByActivo(boolean activo);
}
