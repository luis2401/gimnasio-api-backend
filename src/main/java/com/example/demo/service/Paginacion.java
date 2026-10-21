package com.example.demo.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Construye Pageable con límites seguros (página >= 0, tamaño entre 1 y 100). */
final class Paginacion {

    static final int TAMANO_MAXIMO = 100;

    private Paginacion() {
    }

    static Pageable de(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), TAMANO_MAXIMO), sort);
    }
}
