package com.example.demo.controller;

import com.example.demo.Model.AsignacionSedeModel;
import com.example.demo.dto.CreateAsignacionSedeRequest;
import com.example.demo.dto.FinalizarAsignacionRequest;
import com.example.demo.service.AsignacionSedeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/asignaciones")
public class AsignacionSedeController {

    @Autowired
    private AsignacionSedeService asignacionService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AsignacionSedeModel> obtenerAsignaciones(@RequestParam(required = false) Integer empleadoId,
                                                         @RequestParam(required = false) Integer sedeId) {
        return asignacionService.buscar(empleadoId, sedeId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AsignacionSedeModel obtenerPorId(@PathVariable Integer id) {
        return asignacionService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsignacionSedeModel asignar(@Valid @RequestBody CreateAsignacionSedeRequest request) {
        return asignacionService.asignar(request);
    }

    @PatchMapping("/{id}/finalizar")
    @ResponseStatus(HttpStatus.OK)
    public AsignacionSedeModel finalizar(@PathVariable Integer id,
                                         @RequestBody(required = false) FinalizarAsignacionRequest request) {
        return asignacionService.finalizar(id, request);
    }

    @PatchMapping("/{id}/anular")
    @ResponseStatus(HttpStatus.OK)
    public AsignacionSedeModel anular(@PathVariable Integer id) {
        return asignacionService.anular(id);
    }
}
