package com.example.demo.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        String msg = ex.getReason() != null && !ex.getReason().isBlank()
                ? ex.getReason()
                : "La operación solicitada no pudo completarse.";
        int status = ex.getStatusCode().value();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status);
        body.put("mensaje", msg);
        body.put("message", msg);
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String msg = fieldErrors.stream()
                .map(fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : ("Campo inválido: " + fe.getField()))
                .distinct()
                .collect(Collectors.joining(" • "));
        if (msg.isBlank()) {
            msg = "Verifique que todos los campos obligatorios estén completos y sean válidos.";
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", msg);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = "Falta el parámetro obligatorio '" + ex.getParameterName() + "' en la consulta.";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", msg);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleUnreadablePayload(Exception ex) {
        String msg = "El formato de los datos enviados no es válido. Revise los importes numéricos, fechas y opciones seleccionadas.";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", msg);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        String msg = "No se pudo completar la operación porque infringe una restricción de integridad o registro duplicado en la base de datos.";
        if (raw != null) {
            String lower = raw.toLowerCase();
            if (lower.contains("dni")) {
                msg = "El DNI ingresado ya existe o no tiene el formato válido de 8 dígitos.";
            } else if (lower.contains("telefono")) {
                msg = "El teléfono ingresado no cumple con el formato válido de 9 dígitos.";
            } else if (lower.contains("correo")) {
                msg = "El correo electrónico ingresado ya está registrado o no tiene un formato válido.";
            } else if (lower.contains("codigo")) {
                msg = "El código ingresado ya se encuentra registrado.";
            } else if (lower.contains("foreign key") || lower.contains("violates foreign key")) {
                msg = "No se puede eliminar ni modificar este registro porque tiene operaciones históricas vinculadas (pagos, suscripciones o asistencias).";
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("mensaje", msg);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        String msg = "Acceso denegado: tu rol actual no tiene permisos para realizar esta acción.";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("mensaje", msg);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
