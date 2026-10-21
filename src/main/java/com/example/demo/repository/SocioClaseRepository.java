package com.example.demo.repository;

import com.example.demo.entity.SocioClaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocioClaseRepository extends JpaRepository<SocioClaseEntity, Long> {

    long countByClaseIdAndActivo(Integer claseId, boolean activo);

    boolean existsBySocioIdAndClaseIdAndActivo(Long socioId, Integer claseId, boolean activo);

    List<SocioClaseEntity> findByClaseIdAndActivo(Integer claseId, boolean activo);

    List<SocioClaseEntity> findBySocioId(Long socioId);

    List<SocioClaseEntity> findBySocioIdAndActivo(Long socioId, boolean activo);
}
