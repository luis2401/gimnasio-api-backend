package com.example.demo.repository;

import com.example.demo.entity.EstadoPago;
import com.example.demo.entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<PagoEntity, Integer> {

    List<PagoEntity> findBySuscripcionId(Integer suscripcionId);

    List<PagoEntity> findBySedeIdOrderByFechaPagoDesc(Integer sedeId);

    org.springframework.data.domain.Page<PagoEntity> findBySedeId(
            Integer sedeId, org.springframework.data.domain.Pageable pageable);

    List<PagoEntity> findByFechaPagoBetweenAndEstado(Instant desde, Instant hasta, EstadoPago estado);

    List<PagoEntity> findBySedeIdAndFechaPagoBetweenAndEstado(
            Integer sedeId, Instant desde, Instant hasta, EstadoPago estado);

    List<PagoEntity> findByVendedorIdOrderByFechaPagoDesc(Integer vendedorId);

    List<PagoEntity> findByVendedorIdAndSedeIdOrderByFechaPagoDesc(Integer vendedorId, Integer sedeId);

    org.springframework.data.domain.Page<PagoEntity> findByVendedorId(
            Integer vendedorId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<PagoEntity> findByVendedorIdAndSedeId(
            Integer vendedorId, Integer sedeId, org.springframework.data.domain.Pageable pageable);

    List<PagoEntity> findByVendedorIdAndFechaPagoBetweenAndEstado(
            Integer vendedorId, Instant desde, Instant hasta, EstadoPago estado);

    List<PagoEntity> findByVendedorIdAndSedeIdAndFechaPagoBetweenAndEstado(
            Integer vendedorId, Integer sedeId, Instant desde, Instant hasta, EstadoPago estado);

    boolean existsByNumeroOperacion(String numeroOperacion);
}
