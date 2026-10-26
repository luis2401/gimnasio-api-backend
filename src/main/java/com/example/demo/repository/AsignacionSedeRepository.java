package com.example.demo.repository;

import com.example.demo.entity.AsignacionSedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionSedeRepository extends JpaRepository<AsignacionSedeEntity, Integer> {

    List<AsignacionSedeEntity> findByEmpleadoIdOrderByFechaDesdeDesc(Integer empleadoId);

    List<AsignacionSedeEntity> findBySedeIdOrderByFechaDesdeDesc(Integer sedeId);

    List<AsignacionSedeEntity> findByEmpleadoIdAndSedeIdAndAnulada(Integer empleadoId, Integer sedeId, boolean anulada);

    boolean existsByEmpleadoIdAndSedeIdAndFechaDesde(Integer empleadoId, Integer sedeId, java.time.LocalDate fechaDesde);

    java.util.Optional<AsignacionSedeEntity> findByEmpleadoIdAndSedeIdAndFechaDesde(Integer empleadoId, Integer sedeId, java.time.LocalDate fechaDesde);
}
