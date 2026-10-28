package com.example.demo.repository;

import com.example.demo.entity.EstadoSuscripcion;
import com.example.demo.entity.SuscripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionRepository extends JpaRepository<SuscripcionEntity, Integer> {

    List<SuscripcionEntity> findBySocioIdOrderByFechaInicioDesc(Long socioId);

    List<SuscripcionEntity> findByEstado(EstadoSuscripcion estado);

    org.springframework.data.domain.Page<SuscripcionEntity> findByEstado(
            EstadoSuscripcion estado, org.springframework.data.domain.Pageable pageable);

    long countByEstado(EstadoSuscripcion estado);

    List<SuscripcionEntity> findByEstadoAndFechaFinBetweenOrderByFechaFinAsc(
            EstadoSuscripcion estado, LocalDate desde, LocalDate hasta);

    boolean existsBySocioIdAndEstadoAndFechaFinGreaterThanEqual(Long socioId, EstadoSuscripcion estado, LocalDate fecha);

    boolean existsBySocioIdAndEstadoAndFechaFinGreaterThan(Long socioId, EstadoSuscripcion estado, LocalDate fecha);

    Optional<SuscripcionEntity> findFirstBySocioIdAndEstadoAndFechaFinGreaterThanEqualOrderByFechaFinDesc(
            Long socioId, EstadoSuscripcion estado, LocalDate fecha);

    @Modifying
    @Query("update SuscripcionEntity s set s.estado = com.example.demo.entity.EstadoSuscripcion.VENCIDA "
            + "where s.estado = com.example.demo.entity.EstadoSuscripcion.ACTIVA and s.fechaFin < :hoy")
    int marcarVencidas(@Param("hoy") LocalDate hoy);
}
