package com.example.demo.controller;

import com.example.demo.Model.SedeModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateSedeRequest;
import com.example.demo.dto.UpdateSedeRequest;
import com.example.demo.service.SedeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "https://yanfitness.vercel.app"})
@RestController
@RequestMapping("/sedes")
public class SedeController {

    @Autowired
    private SedeService sedeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<SedeModel> obtenerSedes(@RequestParam(required = false) Boolean soloActivas) {
        if (Boolean.TRUE.equals(soloActivas)) {
            return sedeService.buscarSedesActivas();
        }
        return sedeService.buscarSedes();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SedeModel obtenerSedePorId(@PathVariable Integer id) {
        return sedeService.buscarSedePorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SedeModel crearSede(@Valid @RequestBody CreateSedeRequest request) {
        return sedeService.crearSede(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SedeModel updateSede(@Valid @RequestBody UpdateSedeRequest request, @PathVariable Integer id) {
        return sedeService.updateSede(id, request);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.OK)
    public SedeModel cambiarEstado(@Valid @RequestBody CambiarEstadoRequest request, @PathVariable Integer id) {
        return sedeService.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarSede(@PathVariable Integer id) {
        sedeService.eliminarSede(id);
    }
}
