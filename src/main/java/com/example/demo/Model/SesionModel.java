package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SesionModel {

    private Long id;
    private Long usuarioId;
    private Instant creadaEn;
    private Instant expiraEn;
    private Instant revocadaEn;
    private boolean activa;
}
