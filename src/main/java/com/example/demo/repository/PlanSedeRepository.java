package com.example.demo.repository;

import com.example.demo.entity.PlanSedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanSedeRepository extends JpaRepository<PlanSedeEntity, Integer> {

    List<PlanSedeEntity> findByPlanId(Integer planId);

    List<PlanSedeEntity> findBySedeId(Integer sedeId);

    List<PlanSedeEntity> findBySedeIdAndActivo(Integer sedeId, boolean activo);

    Optional<PlanSedeEntity> findByPlanIdAndSedeId(Integer planId, Integer sedeId);

    boolean existsByPlanIdAndSedeId(Integer planId, Integer sedeId);

    boolean existsByPlanIdAndSedeIdAndIdNot(Integer planId, Integer sedeId, Integer id);
}
