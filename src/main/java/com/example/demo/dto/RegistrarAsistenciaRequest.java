package com.example.demo.dto;

import com.example.demo.entity.EstadoAsistencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrarAsistenciaRequest {

    @NotNull(message = "La inscripción es obligatoria")
    private Long socioClaseId;

    /** Si es null se usa la fecha de hoy. */
    private LocalDate fecha;

    @NotNull(message = "El estado es obligatorio")
    private EstadoAsistencia estado;

    @Size(max = 300, message = "La observación no puede superar los 300 caracteres")
    private String observacion;
}
