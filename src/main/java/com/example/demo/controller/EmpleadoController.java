package com.example.demo.controller;

import com.example.demo.Model.EmpleadoModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateEmpleadoRequest;
import com.example.demo.dto.UpdateEmpleadoRequest;
import com.example.demo.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EmpleadoModel> obtenerEmpleados(@RequestParam(required = false) Boolean soloActivos) {
        return empleadoService.buscarEmpleados(soloActivos);
    }

    @GetMapping("/instructores")
    @ResponseStatus(HttpStatus.OK)
    public List<EmpleadoModel> obtenerInstructores() {
        return empleadoService.buscarInstructores();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmpleadoModel obtenerPorId(@PathVariable Integer id) {
        return empleadoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoModel crearEmpleado(@Valid @RequestBody CreateEmpleadoRequest request) {
        return empleadoService.crearEmpleado(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmpleadoModel updateEmpleado(@PathVariable Integer id, @Valid @RequestBody UpdateEmpleadoRequest request) {
        return empleadoService.updateEmpleado(id, request);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.OK)
    public EmpleadoModel cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoRequest request) {
        return empleadoService.cambiarEstado(id, request);
    }
}
