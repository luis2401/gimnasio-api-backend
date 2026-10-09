package com.example.demo.repository;

import com.example.demo.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Short> {
    Optional<RolEntity> findByCodigo(String codigo);
}
