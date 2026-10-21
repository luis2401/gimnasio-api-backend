package com.example.demo.repository;

import com.example.demo.entity.AsistenciaClaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaClaseRepository extends JpaRepository<AsistenciaClaseEntity, Long> {

    Optional<AsistenciaClaseEntity> findBySocioClaseIdAndFecha(Long socioClaseId, LocalDate fecha);

    List<AsistenciaClaseEntity> findBySocioClaseClaseIdAndFecha(Integer claseId, LocalDate fecha);

    List<AsistenciaClaseEntity> findBySocioClaseIdOrderByFechaDesc(Long socioClaseId);
}
