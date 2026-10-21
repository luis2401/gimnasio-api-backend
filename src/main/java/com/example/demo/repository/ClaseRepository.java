package com.example.demo.repository;

import com.example.demo.entity.ClaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<ClaseEntity, Integer> {

    List<ClaseEntity> findBySedeId(Integer sedeId);

    List<ClaseEntity> findByInstructorId(Integer instructorId);

    List<ClaseEntity> findByActivo(boolean activo);

    List<ClaseEntity> findByInstructorIdAndDiaSemanaAndActivo(Integer instructorId, Short diaSemana, boolean activo);
}
