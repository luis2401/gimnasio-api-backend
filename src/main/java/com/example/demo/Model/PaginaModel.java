package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/** Envoltorio genérico para respuestas paginadas. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginaModel<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PaginaModel<T> from(Page<T> pagina) {
        return PaginaModel.<T>builder()
                .content(pagina.getContent())
                .page(pagina.getNumber())
                .size(pagina.getSize())
                .totalElements(pagina.getTotalElements())
                .totalPages(pagina.getTotalPages())
                .build();
    }
}
