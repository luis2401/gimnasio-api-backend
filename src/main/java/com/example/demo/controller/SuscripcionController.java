package com.example.demo.controller;

import com.example.demo.Model.PagoModel;
import com.example.demo.Model.SuscripcionModel;
import com.example.demo.Model.VentaMembresiaModel;
import com.example.demo.dto.CambiarEstadoSuscripcionRequest;
import com.example.demo.dto.CreateSuscripcionRequest;
import com.example.demo.entity.EstadoSuscripcion;
import com.example.demo.service.SuscripcionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/suscripciones")
public class SuscripcionController {

    @Autowired
    private SuscripcionService suscripcionService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<SuscripcionModel> obtenerSuscripciones(@RequestParam(required = false) Long socioId,
                                                       @RequestParam(required = false) EstadoSuscripcion estado) {
        return suscripcionService.buscarSuscripciones(socioId, estado);
    }

    @GetMapping("/pagina")
    @ResponseStatus(HttpStatus.OK)
    public com.example.demo.Model.PaginaModel<SuscripcionModel> obtenerPagina(
            @RequestParam(required = false) EstadoSuscripcion estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return suscripcionService.buscarPagina(estado, page, size);
    }

    @GetMapping("/por-vencer")
    @ResponseStatus(HttpStatus.OK)
    public List<SuscripcionModel> obtenerPorVencer(@RequestParam(defaultValue = "7") int dias) {
        return suscripcionService.buscarPorVencer(dias);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SuscripcionModel obtenerPorId(@PathVariable Integer id) {
        return suscripcionService.buscarPorId(id);
    }

    @GetMapping("/socio/{socioId}/vigente")
    @ResponseStatus(HttpStatus.OK)
    public SuscripcionModel obtenerVigenteDeSocio(@PathVariable Long socioId) {
        return suscripcionService.obtenerVigenteDeSocio(socioId);
    }

    @GetMapping("/{id}/pagos")
    @ResponseStatus(HttpStatus.OK)
    public List<PagoModel> obtenerPagos(@PathVariable Integer id) {
        return suscripcionService.obtenerPagos(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaMembresiaModel crearSuscripcion(@Valid @RequestBody CreateSuscripcionRequest request) {
        return suscripcionService.crearSuscripcion(request);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.OK)
    public SuscripcionModel cambiarEstado(@PathVariable Integer id,
                                          @Valid @RequestBody CambiarEstadoSuscripcionRequest request) {
        return suscripcionService.cambiarEstado(id, request);
    }
}
