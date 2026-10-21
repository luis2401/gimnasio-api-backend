package com.example.demo.controller;

import com.example.demo.Model.AsistenciaClaseModel;
import com.example.demo.Model.SocioClaseModel;
import com.example.demo.dto.InscribirSocioRequest;
import com.example.demo.dto.RegistrarAsistenciaRequest;
import com.example.demo.service.AsistenciaService;
import com.example.demo.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin("http://localhost:3000/")
@RestController
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private AsistenciaService asistenciaService;

    @GetMapping("/inscripciones")
    @ResponseStatus(HttpStatus.OK)
    public List<SocioClaseModel> obtenerPorSocio(@RequestParam Long socioId,
                                                 @RequestParam(required = false) Boolean soloActivas) {
        return inscripcionService.buscarPorSocio(socioId, soloActivas);
    }

    @PostMapping("/inscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    public SocioClaseModel inscribir(@Valid @RequestBody InscribirSocioRequest request) {
        return inscripcionService.inscribir(request);
    }

    @PatchMapping("/inscripciones/{id}/baja")
    @ResponseStatus(HttpStatus.OK)
    public SocioClaseModel darDeBaja(@PathVariable Long id) {
        return inscripcionService.darDeBaja(id);
    }

    @GetMapping("/inscripciones/{id}/asistencias")
    @ResponseStatus(HttpStatus.OK)
    public List<AsistenciaClaseModel> obtenerAsistenciasDeInscripcion(@PathVariable Long id) {
        return asistenciaService.buscarPorInscripcion(id);
    }

    @PostMapping("/asistencias")
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaClaseModel registrarAsistencia(@Valid @RequestBody RegistrarAsistenciaRequest request) {
        return asistenciaService.registrar(request);
    }

    @GetMapping("/asistencias")
    @ResponseStatus(HttpStatus.OK)
    public List<AsistenciaClaseModel> obtenerAsistenciasDeClase(
            @RequestParam Integer claseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return asistenciaService.buscarPorClaseYFecha(claseId, fecha);
    }
}
