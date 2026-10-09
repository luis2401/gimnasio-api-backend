package com.example.demo.controller;

import com.example.demo.Model.PlanMembresiaModel;
import com.example.demo.Model.PlanSedeModel;
import com.example.demo.dto.AsignarPrecioSedeRequest;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreatePlanMembresiaRequest;
import com.example.demo.dto.UpdatePlanMembresiaRequest;
import com.example.demo.service.PlanMembresiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/planes")
public class PlanMembresiaController {

    @Autowired
    private PlanMembresiaService planService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PlanMembresiaModel> obtenerPlanes(@RequestParam(required = false) Boolean soloActivos) {
        return planService.buscarPlanes(soloActivos);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlanMembresiaModel obtenerPlanPorId(@PathVariable Integer id) {
        return planService.buscarPlanPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanMembresiaModel crearPlan(@Valid @RequestBody CreatePlanMembresiaRequest request) {
        return planService.crearPlan(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlanMembresiaModel actualizarPlan(@PathVariable Integer id, @Valid @RequestBody UpdatePlanMembresiaRequest request) {
        return planService.actualizarPlan(id, request);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.OK)
    public PlanMembresiaModel cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoRequest request) {
        return planService.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPlan(@PathVariable Integer id) {
        planService.eliminarPlan(id);
    }

    @PostMapping("/{id}/precios-sede")
    @ResponseStatus(HttpStatus.OK)
    public PlanSedeModel asignarOActualizarPrecioSede(@PathVariable Integer id, @Valid @RequestBody AsignarPrecioSedeRequest request) {
        return planService.asignarOActualizarPrecioSede(id, request);
    }

    @GetMapping("/{id}/precios-sede")
    @ResponseStatus(HttpStatus.OK)
    public List<PlanSedeModel> obtenerPreciosPorPlan(@PathVariable Integer id) {
        return planService.obtenerPreciosPorPlan(id);
    }

    @GetMapping("/sede/{sedeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<PlanSedeModel> obtenerPlanesPorSede(@PathVariable Integer sedeId, @RequestParam(required = false) Boolean soloActivos) {
        return planService.obtenerPlanesPorSede(sedeId, soloActivos);
    }
}
