package com.example.demo.controller;

import com.example.demo.Model.PagoModel;
import com.example.demo.dto.AnularPagoRequest;
import com.example.demo.dto.CreatePaseDiarioRequest;
import com.example.demo.dto.ReporteIngresos;
import com.example.demo.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PagoModel> obtenerPagos(@RequestParam(required = false) Integer sedeId) {
        return pagoService.buscarPagos(sedeId);
    }

    @GetMapping("/reporte")
    @ResponseStatus(HttpStatus.OK)
    public ReporteIngresos obtenerReporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer sedeId) {
        return pagoService.generarReporte(desde, hasta, sedeId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PagoModel obtenerPorId(@PathVariable Integer id) {
        return pagoService.buscarPorId(id);
    }

    @PostMapping("/pase-diario")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoModel registrarPaseDiario(@Valid @RequestBody CreatePaseDiarioRequest request) {
        return pagoService.registrarPaseDiario(request);
    }

    @PatchMapping("/{id}/anular")
    @ResponseStatus(HttpStatus.OK)
    public PagoModel anularPago(@PathVariable Integer id, @Valid @RequestBody AnularPagoRequest request) {
        return pagoService.anularPago(id, request);
    }
}
