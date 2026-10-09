package com.example.demo.repository;

import com.example.demo.entity.EmpleadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<EmpleadoEntity, Integer> {
    Optional<EmpleadoEntity> findByDni(String dni);
}
