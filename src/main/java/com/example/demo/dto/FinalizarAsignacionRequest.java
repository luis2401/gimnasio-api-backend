package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalizarAsignacionRequest {

    /** Si es null se usa la fecha de hoy. */
    private LocalDate fechaHasta;
}
