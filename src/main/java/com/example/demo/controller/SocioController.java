package com.example.demo.controller;

import com.example.demo.Model.SocioModel;
import com.example.demo.dto.CambiarEstadoRequest;
import com.example.demo.dto.CreateSocioRequest;
import com.example.demo.dto.ReporteSocio;
import com.example.demo.dto.UpdateSocioRequest;
import com.example.demo.service.SocioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gimnasio")
public class SocioController {

    @Autowired
    private SocioService socioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<SocioModel> obtenerSocios() {
        return socioService.buscarSocios();
    }

    @GetMapping("/pagina")
    @ResponseStatus(HttpStatus.OK)
    public com.example.demo.Model.PaginaModel<SocioModel> obtenerSociosPagina(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return socioService.buscarSociosPagina(q, page, size);
    }

    @GetMapping("/cantidad")
    @ResponseStatus(HttpStatus.OK)
    public long obtenerCantidadSocios() {
        return socioService.cantidadSocios();
    }

    @GetMapping("/reporte")
    @ResponseStatus(HttpStatus.OK)
    public ReporteSocio obtenerReporte() {
        return socioService.generarReporte();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SocioModel obtenerSocioPorId(@PathVariable Long id) {
        return socioService.buscarSocioPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SocioModel crearSocio(@Valid @RequestBody CreateSocioRequest createSocioRequest) {
        return socioService.crearSocio(createSocioRequest);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SocioModel updateSocio(@Valid @RequestBody UpdateSocioRequest updateSocioRequest, @PathVariable Long id) {
        return socioService.updateSocio(id, updateSocioRequest);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.OK)
    public SocioModel cambiarEstado(@Valid @RequestBody CambiarEstadoRequest request, @PathVariable Long id) {
        return socioService.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarSocio(@PathVariable Long id) {
        socioService.eliminarSocio(id);
    }
}
