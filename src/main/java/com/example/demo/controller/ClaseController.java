package com.example.demo.controller;

import com.example.demo.Model.ClaseModel;
import com.example.demo.Model.SocioClaseModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateClaseRequest;
import com.example.demo.dto.UpdateClaseRequest;
import com.example.demo.service.ClaseService;
import com.example.demo.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private InscripcionService inscripcionService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ClaseModel> obtenerClases(@RequestParam(required = false) Integer sedeId,
                                          @RequestParam(required = false) Integer instructorId,
                                          @RequestParam(required = false) Boolean soloActivas) {
        return claseService.buscarClases(sedeId, instructorId, soloActivas);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClaseModel obtenerPorId(@PathVariable Integer id) {
        return claseService.buscarPorId(id);
    }

    @GetMapping("/{id}/inscripciones")
    @ResponseStatus(HttpStatus.OK)
    public List<SocioClaseModel> obtenerInscripciones(@PathVariable Integer id,
                                                      @RequestParam(required = false) Boolean soloActivas) {
        return inscripcionService.buscarPorClase(id, soloActivas);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaseModel crearClase(@Valid @RequestBody CreateClaseRequest request) {
        return claseService.crearClase(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ClaseModel updateClase(@PathVariable Integer id, @Valid @RequestBody UpdateClaseRequest request) {
        return claseService.updateClase(id, request);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.OK)
    public ClaseModel cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoRequest request) {
        return claseService.cambiarEstado(id, request);
    }
}
